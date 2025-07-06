package whoacommunity.components.admin;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.data.Article;

public class WCArticleEditPage extends WCComponent {

	public Article selectedObject;

	public WCArticleEditPage( NGContext context ) {
		super( context );
	}

	public NGActionResults saveChanges() {
		selectedObject.getObjectContext().commitChanges();
		return null;
	}
}