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

		routeTable().map( "/", WCMain.class );
		routeTable().map( "/slack-archive", WCSlackArchivePage.class );
		routeTable().map( "/slack-client", WCSlackClientPage.class );
	}
}