package whoacommunity.components;

import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.app.WCCore;
import whoacommunity.components.admin.WCArticleEditPage;
import whoacommunity.data.Article;

public class WCMain extends WCComponent {

	public Article currentArticle;

	public WCMain( NGContext context ) {
		super( context );
	}

	public List<Article> articles() {
		final ObjectSelect<Article> query = ObjectSelect
				.query( Article.class )
				.orderBy( Article.DATE.desc() );

		if( !showAdminStuff() ) {
			query.where( Article.PUBLISHED.isTrue() );
		}

		return query
				.select( WCCore.newContext() );
	}

	public NGActionResults editArticle() {
		final WCArticleEditPage nextPage = pageWithName( WCArticleEditPage.class );
		nextPage.selectedObject = currentArticle;
		return nextPage;
	}

	public String currentArticleURL() {
		return "/article/%s".formatted( currentArticle.uniqueID() );
	}
}