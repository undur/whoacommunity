package whoacommunity.app;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategyFactory;
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
		System.clearProperty( "wc.jdbcURL" );
		//		System.setProperty( "org.slf4j.simpleLogger.log.org.apache.cayenne.access.QueryLogger", "DEBUG" );
		//		System.setProperty( "org.slf4j.simpleLogger.log.org.apache.cayenne", "DEBUG" );
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
			final CayenneRuntimeBuilder builder = CayenneRuntime
					.builder()
					.addConfig( "cayenne/cayenne-project.xml" );

			final HikariConfig config = new HikariConfig();

			if( jdbcURL() != null ) {
				// If jdbcURL is set, use connection data from environment
				config.setJdbcUrl( jdbcURL() );
				config.setUsername( username() );
				config.setPassword( password() );
				config.setMaximumPoolSize( 4 );
			}
			else {
				// If no jdbcURL is set, create and use a new in-memory h2 DB.
				// We override the SchemaUpdateStrategyFactory rather than binding SchemaUpdateStrategy directly:
				// DefaultSchemaUpdateStrategyFactory reads the strategy from the DataNodeDescriptor (the
				// schema-update-strategy attribute in cayenne-project.xml) and instantiates it reflectively,
				// never consulting a SchemaUpdateStrategy DI binding. Binding the factory is the only way to
				// control the strategy from code. Hugi 2026-05-26
				builder.addModule( b -> b.bind( SchemaUpdateStrategyFactory.class ).toInstance( _ -> new CreateIfNoSchemaStrategy() ) );

				config.setDriverClassName( "org.h2.Driver" );
				config.setJdbcUrl( "jdbc:h2:mem:testerbest" );
			}

			_runtime = builder
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