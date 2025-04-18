package whoacommunity.app;

import ng.appserver.NGApplication;
import ng.plugins.Elements;
import ng.plugins.Routes;
import whoacommunity.components.WCMain;
import whoacommunity.components.WCSlackArchivePage;
import whoacommunity.components.WCSlackClientPage;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	@Override
	public Elements elements() {
		return Elements
				.create()
				.elementPackage( "whoacommunity.components" );
	}

	@Override
	public Routes routes() {
		return Routes
				.create()
				.map( "/", WCMain.class )
				.map( "/slack-archive", WCSlackArchivePage.class )
				.map( "/slack-client", WCSlackClientPage.class );
	}
}