package whoacommunity.util;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A value with a TTL, kept fresh by a background ticker rather than by
 * unlucky visitors: {@link #startBackgroundRefresh(Duration)} schedules a
 * periodic sweep that refreshes every feed whose TTL has expired, in
 * parallel on virtual threads. Reads are stale-while-revalidate — a
 * {@code value()} call never blocks once a value exists; if it finds
 * itself stale (e.g. in the window before the next tick) it returns the
 * stale value immediately and kicks a refresh in the background. The
 * only blocking read is the very first one after boot, before any data
 * has ever loaded — and the boot-time sweep usually wins that race.
 *
 * On a refresh failure the previously held value is kept and the failure
 * is logged — callers see stale data rather than empty/exception.
 */
public class CachedFeed<T> {

	private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
	private static final List<CachedFeed<?>> REGISTRY = new CopyOnWriteArrayList<>();
	private static final Duration FAN_OUT_TIMEOUT = Duration.ofSeconds( 10 );

	private static ScheduledExecutorService _scheduler;

	private final Duration _cacheDuration;
	private final Supplier<T> _supplier;

	/** Guards _value and _lastRefreshed; never held while running the supplier. */
	private final ReentrantLock _lock = new ReentrantLock();
	private Instant _lastRefreshed;
	private T _value;

	public CachedFeed( final Duration cacheDuration, final Supplier<T> supplier ) {
		this( cacheDuration, supplier, null );
	}

	/**
	 * @param initial Value returned before the first successful refresh.
	 *                Useful when callers prefer e.g. an empty list to a null.
	 */
	public CachedFeed( final Duration cacheDuration, final Supplier<T> supplier, final T initial ) {
		_cacheDuration = cacheDuration;
		_supplier = supplier;
		_value = initial;
		REGISTRY.add( this );
	}

	public T value() {
		if( neverRefreshed() ) {
			// First-ever read (boot, before the background sweep has run):
			// block once so we don't render empty feeds.
			refreshAllStale();
		}
		else if( isStale() ) {
			// Stale-while-revalidate: serve the stale value now, freshen in
			// the background for the next reader.
			CompletableFuture.runAsync( CachedFeed::refreshAllStale, EXECUTOR );
		}

		return readValue();
	}

	private boolean neverRefreshed() {
		_lock.lock();
		try {
			return _lastRefreshed == null;
		}
		finally {
			_lock.unlock();
		}
	}

	private boolean isStale() {
		_lock.lock();
		try {
			if( _lastRefreshed == null || _cacheDuration == null ) {
				return true;
			}
			return Duration.between( _lastRefreshed, Instant.now() ).compareTo( _cacheDuration ) > 0;
		}
		finally {
			_lock.unlock();
		}
	}

	private T readValue() {
		_lock.lock();
		try {
			return _value;
		}
		finally {
			_lock.unlock();
		}
	}

	private void doRefresh() {
		T fresh;
		try {
			fresh = _supplier.get();
		}
		catch( Exception e ) {
			System.err.println( "CachedFeed refresh failed: " + e.getMessage() );
			e.printStackTrace();

			// Mark refreshed even on failure to avoid hammering the source on every call
			_lock.lock();
			try {
				_lastRefreshed = Instant.now();
			}
			finally {
				_lock.unlock();
			}
			return;
		}

		_lock.lock();
		try {
			_value = fresh;
			_lastRefreshed = Instant.now();
		}
		finally {
			_lock.unlock();
		}
	}

	/**
	 * Submit every currently-stale feed's refresh to the virtual-thread
	 * executor and wait for all of them to finish (with a hard timeout
	 * so a slow source can't hang a page render).
	 */
	private static void refreshAllStale() {
		fanOut( false );
	}

	/**
	 * Force-refresh <em>every</em> registered feed regardless of TTL, in
	 * parallel. Intended for a manual "refresh now" trigger. Blocks until
	 * all refreshes finish or the fan-out timeout elapses.
	 */
	public static void forceRefreshAll() {
		fanOut( true );
	}

	/**
	 * Start the background sweep: every {@code interval}, refresh all feeds
	 * whose TTL has expired, off the request path. The first sweep runs
	 * immediately, warming caches at boot. Idempotent — extra calls are
	 * ignored. Runs on a daemon thread so it never blocks JVM shutdown.
	 *
	 * Note that a feed only joins the sweep once its holding class has
	 * loaded (registration happens in the constructor), so eager feeds
	 * should be class-loaded at startup — see Application.
	 */
	public static synchronized void startBackgroundRefresh( final Duration interval ) {
		if( _scheduler != null ) {
			return;
		}

		_scheduler = Executors.newSingleThreadScheduledExecutor( runnable -> {
			final Thread thread = new Thread( runnable, "CachedFeed-background-refresh" );
			thread.setDaemon( true );
			return thread;
		} );

		_scheduler.scheduleWithFixedDelay( CachedFeed::refreshAllStale, 0, interval.toMillis(), TimeUnit.MILLISECONDS );
	}

	private static void fanOut( final boolean force ) {
		final List<CompletableFuture<Void>> futures = new ArrayList<>();
		for( CachedFeed<?> feed : REGISTRY ) {
			if( force || feed.isStale() ) {
				futures.add( CompletableFuture.runAsync( feed::doRefresh, EXECUTOR ) );
			}
		}

		if( futures.isEmpty() ) {
			return;
		}

		try {
			CompletableFuture
					.allOf( futures.toArray( new CompletableFuture<?>[ 0 ] ) )
					.get( FAN_OUT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS );
		}
		catch( Exception e ) {
			System.err.println( "CachedFeed fan-out timed out or failed: " + e.getMessage() );
		}
	}
}
