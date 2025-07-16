package whoacommunity.components.admin;

import java.time.LocalDate;

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

	/**
	 * FIXME: Hmmm... KVC should really just handle ISO-8601 string/date conversion automatically, much like numeric conversions // Hugi 2025-07-16
	 */
	public String stringDate() {
		return selectedObject.date().toString();
	}

	public void setStringDate( String string ) {
		selectedObject.setDate( LocalDate.parse( string ) );
	}

	/**
	 * FIXME: Sigh... Spend those couple of minutes and add an NGChecbox element. This is embarrassing // Hugi 2025-07-16
	 */
	public String stringPublished() {
		return selectedObject.published() ? "1" : "0";
	}

	public void setStringPublished( String string ) {
		selectedObject.setPublished( string.equals( "1" ) );
	}
}