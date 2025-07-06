package whoacommunity.components.admin;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.data.Article;
import whoacommunity.data.Article.ArticleFormat;

public class WCArticleEditPage extends WCComponent {

	public Article selectedObject;

	public WCArticleEditPage( NGContext context ) {
		super( context );
	}

	public NGActionResults saveChanges() {
		selectedObject.getObjectContext().commitChanges();
		return null;
	}

	public ArticleFormat[] articleFormats() {
		return ArticleFormat.values();
	}
}