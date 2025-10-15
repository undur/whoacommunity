package whoacommunity.components;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.components.admin.WCArticleEditPage;

public class WCMain extends WCComponent {

	public WCMain( NGContext context ) {
		super( context );
	}

	public NGActionResults editArticle() {
		final WCArticleEditPage nextPage = pageWithName( WCArticleEditPage.class );
		nextPage.selectedObject = currentArticle;
		return nextPage;
	}
}