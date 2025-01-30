package whoacommunity.app;

import ng.appserver.NGApplication;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		routeTable().map( "/", request -> {
			return pageWithName( "Blorp", request.context() );
		} );
	}
}