package whoacommunity.app;

import ng.appserver.NGApplication;
import ng.appserver.NGElementUtils;
import whoacommunity.components.WCLook;

public class Application extends NGApplication {

	static {
		NGElementUtils.addClass( WCLook.class );
	}

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		routeTable().map( "/", request -> {
			return pageWithName( "Blorp", request.context() );
		} );
	}
}