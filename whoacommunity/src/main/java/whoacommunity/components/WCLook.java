package whoacommunity.components;


import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The page frame: sidebar with brand and site tree, main column with
 * breadcrumb bar, content and footer. Page identity (breadcrumbs, current
 * section) is read from the page component being wrapped.
 */
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
	}

	public String envString() {
		return "Góður kóði slf. 2026";
	}

	public NGActionResults search() {
		final WCSearchPage nextPage = pageWithName( WCSearchPage.class );
		nextPage.searchString = searchString;
		nextPage.search();
		return nextPage;
	}

}
