package whoacommunity.util;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A value with a TTL: calls the supplier when first accessed and again
 * once the cache window has passed, otherwise returns the previous value.
 *
 * On a refresh failure the previously held value is kept and the failure
 * is logged — callers see stale data rather than empty/exception.
 *
 * When a {@code value()} call finds itself stale, it walks the registry
 * of all CachedFeed instances and refreshes <em>every</em> stale feed in
 * parallel before returning. The current caller therefore pays the cost
 * of the slowest stale feed once, but subsequent reads in the same
 * render hit warm caches instantly. Suppliers run on virtual threads.
 */
public class CachedFeed<T> {

	private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
	private static final List<CachedFeed<?>> REGISTRY = new CopyOnWriteArrayList<>();
	private static final Duration FAN_OUT_TIMEOUT = Duration.ofSeconds( 10 );

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
		if( isStale() ) {
			refreshAllStale();
		}
		return readValue();
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
		final List<CompletableFuture<Void>> futures = new ArrayList<>();
		for( CachedFeed<?> feed : REGISTRY ) {
			if( feed.isStale() ) {
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
