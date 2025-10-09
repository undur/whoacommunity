package whoacommunity.components;

import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.app.WCCore;
import whoacommunity.components.WCFeedPage.OurFeed.OurItem;
import whoacommunity.components.admin.WCArticleEditPage;
import whoacommunity.data.Article;

public class WCMain extends WCComponent {

	public Article currentArticle;
	private List<Article> _articles;
	public OurItem current;

	public WCMain( NGContext context ) {
		super( context );
	}

	public List<Article> articles() {
		if( _articles == null ) {
			final ObjectSelect<Article> query = ObjectSelect
					.query( Article.class )
					.orderBy( Article.DATE.desc() );

			if( !showAdminStuff() ) {
				query.where( Article.PUBLISHED.isTrue() );
			}

			_articles = query.select( WCCore.newContext() );
		}

		return _articles;
	}

	public NGActionResults editArticle() {
		final WCArticleEditPage nextPage = pageWithName( WCArticleEditPage.class );
		nextPage.selectedObject = currentArticle;
		return nextPage;
	}

	public String currentArticleURL() {
		return "/article/%s".formatted( currentArticle.uniqueID() );

	}

	/**
	 * FIXME: Yeah, we shouldn't be storing that feed in the dev page. Lazy // Hugi 2025-10-09
	 */
	public List<OurItem> items() {
		return WCFeedPage.feed.items().subList( 0, 10 );
	}
}