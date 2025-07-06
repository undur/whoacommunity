package whoacommunity.components.admin;

import org.apache.cayenne.ObjectContext;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.NGResponse;
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

	public NGActionResults delete() {
		final ObjectContext oc = selectedObject.getObjectContext();
		oc.deleteObject( selectedObject );
		oc.commitChanges();
		return redirectTemporary( "/" );
	}

	public ArticleFormat[] articleFormats() {
		return ArticleFormat.values();
	}

	/**
	 * Creates a response with a temporary (302) redirect to the specified URL
	 *
	 * @param url The URL to redirect to
	 */
	private static NGResponse redirectTemporary( final String url ) {
		final NGResponse response = new NGResponse();
		response.setStatus( 302 );
		response.setHeader( "location", url );
		response.setHeader( "content-type", "text/html" );
		return response;
	}
}