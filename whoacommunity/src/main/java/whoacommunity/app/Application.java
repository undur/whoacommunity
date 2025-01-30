package whoacommunity.app;

import ng.appserver.NGApplication;
import ng.appserver.NGElementUtils;
import whoacommunity.components.WCMain;

public class Application extends NGApplication {

	static {
		NGElementUtils.addPackage( "whoacommunity.components" );
	}

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		routeTable().map( "/", request -> {
			return pageWithName( WCMain.class, request.context() );
		} );
	}
}