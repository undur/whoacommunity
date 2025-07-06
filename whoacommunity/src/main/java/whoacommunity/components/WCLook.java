package whoacommunity.components;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.data.SlackUser;

public class WCLook extends NGComponent {

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
}