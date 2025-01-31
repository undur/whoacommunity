package whoacommunity.app;

import ng.appserver.NGApplication;
import ng.appserver.NGElementUtils;
import whoacommunity.components.WCMain;
import whoacommunity.components.WCSlackArchivePage;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		NGElementUtils.addPackage( "whoacommunity.components" );

		routeTable().mapComponent( "/", WCMain.class );
		routeTable().mapComponent( "/slack-archive", WCSlackArchivePage.class );
	}
}