package whoacommunity.components;

import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.app.WCCore;
import whoacommunity.data.Article;

public class WCMain extends NGComponent {

	public Article currentArticle;

	public WCMain( NGContext context ) {
		super( context );
	}

	public List<Article> articles() {
		return ObjectSelect
				.query( Article.class )
				.orderBy( Article.DATE.desc() )
				.select( WCCore.newContext() );
	}
}