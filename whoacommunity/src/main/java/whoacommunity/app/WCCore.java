package whoacommunity.app;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.reflect.NonPrefixedBeanAccessor;
import org.apache.cayenne.runtime.CayenneRuntime;
import org.apache.cayenne.runtime.CayenneRuntimeBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jambalaya.listeners.DateTimestampedListener;
import jambalaya.listeners.UUIDStampedListener;
import jambalaya.listeners.UniqueIDStampedListener;

/**
 * The basics for accessing the DB through Cayenne
 */

public class WCCore {

	static {
		NonPrefixedBeanAccessor.register();
	}

	private static CayenneRuntime _serverRuntime;

	/**
	 * @return A newly constructed ObjectContext
	 */
	public static ObjectContext newContext() {
		return serverRuntime().newContext();
	}

	public static CayenneRuntime serverRuntime() {
		if( _serverRuntime == null ) {
			CayenneRuntimeBuilder b = CayenneRuntime.builder( "WhoaCommunity" );
			b.addConfig( "cayenne/cayenne-project.xml" );

			HikariConfig config = new HikariConfig();
			config.setJdbcUrl( jdbcURL() );
			config.setUsername( username() );
			config.setPassword( password() );
			config.setMaximumPoolSize( 4 );
			HikariDataSource dataSource = new HikariDataSource( config );
			b = b.dataSource( dataSource );

			_serverRuntime = b.build();

			_serverRuntime.getDataDomain().addListener( new DateTimestampedListener() );
			_serverRuntime.getDataDomain().addListener( new UniqueIDStampedListener() );
			_serverRuntime.getDataDomain().addListener( new UUIDStampedListener() );
		}

		return _serverRuntime;
	}

	/**
	 * @return The jdbcURL property
	 */
	private static String jdbcURL() {
		return System.getProperty( "wc.jdbcURL" );
	}

	/**
	 * @return The jdbcURL property
	 */
	private static String username() {
		return System.getProperty( "wc.username" );
	}

	/**
	 * @return The jdbcURL property
	 */
	private static String password() {
		return System.getProperty( "wc.password" );
	}
}