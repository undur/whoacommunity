package whoacommunity.app;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.reflect.NonPrefixedBeanAccessor;
import org.apache.cayenne.runtime.CayenneRuntime;
import org.apache.cayenne.runtime.CayenneRuntimeBuilder;

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
		//		System.clearProperty( "wc.jdbcURL" );
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
			final CayenneRuntimeBuilder builder = CayenneRuntime.builder();

			final HikariConfig config = new HikariConfig();

			if( jdbcURL() != null ) {
				// If jdbcURL is set, use connection data from environment
				config.setJdbcUrl( jdbcURL() );
				config.setUsername( username() );
				config.setPassword( password() );
				config.setMaximumPoolSize( 4 );
			}
			else {
				// If no jdbcURL is set, create and use a new in-memory h2 DB
				// FIXME: Schema generation strategy doesn't seem to be taking effect. Check out // Hugi 2026-05-16
				builder.addModule( b -> b.bind( SchemaUpdateStrategy.class ).to( CreateIfNoSchemaStrategy.class ) );
				config.setDriverClassName( "org.h2.Driver" );
				config.setJdbcUrl( "jdbc:h2:mem:testerbest" );
			}

			_runtime = builder
					.addConfig( "cayenne/cayenne-project.xml" )
					.dataSource( new HikariDataSource( config ) )
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

	/**
	 * @return The GitHub API token used by the GitHub feed (classic PAT, public_repo scope is sufficient)
	 */
	public static String githubToken() {
		return System.getProperty( "wc.githubToken" );
	}
}