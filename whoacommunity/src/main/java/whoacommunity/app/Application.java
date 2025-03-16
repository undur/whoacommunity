package whoacommunity.app;

import ng.appserver.NGApplication;
import whoacommunity.components.WCMain;
import whoacommunity.components.WCSlackArchivePage;
import whoacommunity.components.WCSlackClientPage;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		elementManager().registerElementPackage( "whoacommunity.components" );

		routeTable().mapComponent( "/", WCMain.class );
		routeTable().mapComponent( "/slack-archive", WCSlackArchivePage.class );
		routeTable().mapComponent( "/slack-client", WCSlackClientPage.class );
	}
}