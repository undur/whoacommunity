package whoacommunity.app;

import java.util.UUID;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGActionResults;
import ng.appserver.NGApplication;
import ng.appserver.NGRequest;
import ng.plugins.Elements;
import ng.plugins.Routes;
import whoacommunity.components.WCArticleDetailPage;
import whoacommunity.components.WCDeploymentPage;
import whoacommunity.components.WCFeedPage;
import whoacommunity.components.WCMain;
import whoacommunity.components.WCSlackArchivePage;
import whoacommunity.components.WCSlackClientPage;
import whoacommunity.data.Article;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	@Override
	public Elements elements() {
		return Elements
				.create()
				.elementPackage( "whoacommunity.components" )
				.elementPackage( "whoacommunity.components.admin" );
	}

	@Override
	public Routes routes() {
		return Routes
				.create()
				.map( "/", WCMain.class )
				.map( "/slack-archive", WCSlackArchivePage.class )
				.map( "/slack-client", WCSlackClientPage.class )
				.map( "/dev-feed", WCFeedPage.class )
				.map( "/article/*", this::viewArticle )
				.map( "/deployment-config", WCDeploymentPage.class );
	}

	public NGActionResults viewArticle( NGRequest request ) {
		final String uuidString = request.parsedURI().getString( 1 );
		final UUID uuid = UUID.fromString( uuidString );

		final Article article = ObjectSelect
				.query( Article.class )
				.where( Article.UNIQUE_ID.eq( uuid ) )
				.selectOne( WCCore.newContext() );

		final WCArticleDetailPage page = pageWithName( WCArticleDetailPage.class, request.context() );
		page.selectedObject = article;
		return page;
	}
}