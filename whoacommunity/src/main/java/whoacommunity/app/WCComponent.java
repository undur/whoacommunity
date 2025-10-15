package whoacommunity.app;

import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.components.WCFeedPage;
import whoacommunity.components.WCFeedPage.OurFeed.OurItem;
import whoacommunity.data.Article;

public abstract class WCComponent extends NGComponent {

	public WCComponent( NGContext context ) {
		super( context );
	}

	/**
	 * FIXME: A hack to determine whether we show the admin page. Will eventually be controlled through login/access privileges // Hugi 2025-07-06
	 */
	@Deprecated
	public boolean isLocal() {
		return application().isDevelopmentMode();
	}

	public boolean showAdminStuff() {
		return isLocal();
	}

	/* FIXME: The following should really be in a utility class rather than the shared component superclass */

	public Article currentArticle;
	private List<Article> _articles;
	public OurItem current;

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