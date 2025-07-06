package whoacommunity.components;

import java.time.LocalDate;

import org.apache.cayenne.ObjectContext;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.app.WCCore;
import whoacommunity.components.admin.WCAdminPage;
import whoacommunity.data.Article;
import whoacommunity.data.SlackUser;

public class WCLook extends WCComponent {

	public String searchString;

	public WCLook( NGContext context ) {
		super( context );
	}

	/**
	 * FIXME: This should be configurable. Should be null for a non-fluid layout
	 */
	public String bodyClass() {
		return "layout-fluid";
		//		return null;
	}

	/**
	 * FIXME: We currently don't allow logins so this always returns null
	 */
	public SlackUser user() {
		return null;
	}

	public boolean showTopButtons() {
		return false;
	}

	public String envString() {
		return "Góður kóði slf. 2025";
	}

	public NGActionResults search() {
		final WCSearchPage nextPage = pageWithName( WCSearchPage.class );
		nextPage.searchString = searchString;
		nextPage.search();
		return nextPage;
	}

	public NGActionResults admin() {
		return pageWithName( WCAdminPage.class );
	}

	public NGActionResults createArticle() {
		final ObjectContext oc = WCCore.newContext();

		final Article a = oc.newObject( Article.class );
		a.setDate( LocalDate.now() );
		a.setTitle( "New article" );
		a.setContent( "" );

		oc.commitChanges();

		return null;
	}
}