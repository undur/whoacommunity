package whoacommunity.util;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * A value with a TTL: calls the supplier when first accessed and again
 * once the cache window has passed, otherwise returns the previous value.
 *
 * On a refresh failure the previously held value is kept and the failure
 * is logged — callers see stale data rather than empty/exception.
 */
public class CachedFeed<T> {

	private final Duration _cacheDuration;
	private final Supplier<T> _supplier;

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
	}

	public synchronized T value() {
		if( shouldRefresh() ) {
			try {
				_value = _supplier.get();
			}
			catch( Exception e ) {
				System.err.println( "CachedFeed refresh failed: " + e.getMessage() );
				e.printStackTrace();
			}
			// Mark refreshed even on failure so we don't hammer the source on every call
			_lastRefreshed = Instant.now();
		}
		return _value;
	}

	private boolean shouldRefresh() {
		if( _lastRefreshed == null || _cacheDuration == null ) {
			return true;
		}
		return Duration.between( _lastRefreshed, Instant.now() ).compareTo( _cacheDuration ) > 0;
	}
}
