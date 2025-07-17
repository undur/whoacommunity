package whoacommunity.components;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.data.Article;

public class WCArticleDetailPage extends WCComponent {

	public Article selectedObject;

	public WCArticleDetailPage( NGContext context ) {
		super( context );
	}
}