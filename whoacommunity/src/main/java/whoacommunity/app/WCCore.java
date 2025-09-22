package whoacommunity.app;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.reflect.NonPrefixedBeanAccessor;
import org.apache.cayenne.runtime.CayenneRuntime;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jambalaya.listeners.DateTimestampedListener;
import jambalaya.listeners.UUIDStampedListener;

/**
 * The basics for accessing the DB through Cayenne
 */

public class WCCore {

	static {
		NonPrefixedBeanAccessor.register();
	}

	private static CayenneRuntime _runtime;

	/**
	 * @return A newly constructed ObjectContext
	 */
	public static ObjectContext newContext() {
		return runtime().newContext();
	}

	public static CayenneRuntime runtime() {
		if( _runtime == null ) {
			final HikariConfig config = new HikariConfig();
			config.setJdbcUrl( jdbcURL() );
			config.setUsername( username() );
			config.setPassword( password() );
			config.setMaximumPoolSize( 4 );
			final HikariDataSource dataSource = new HikariDataSource( config );

			_runtime = CayenneRuntime
					.builder( "WhoaCommunity" )
					.addConfig( "cayenne/cayenne-project.xml" )
					.dataSource( dataSource )
					.build();

			_runtime.getDataDomain().addListener( new DateTimestampedListener() );
			_runtime.getDataDomain().addListener( new UUIDStampedListener() );
		}

		return _runtime;
	}

	/**
	 * @return The jdbcURL property
	 */
	private static String jdbcURL() {
		return System.getProperty( "wc.jdbcURL" );
	}

	/**
	 * @return The username property
	 */
	private static String username() {
		return System.getProperty( "wc.username" );
	}

	/**
	 * @return The password property
	 */
	private static String password() {
		return System.getProperty( "wc.password" );
	}
}