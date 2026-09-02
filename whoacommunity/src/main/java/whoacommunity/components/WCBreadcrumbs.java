package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The breadcrumb trail in the top bar. Like the site tree, it reads the
 * trail from the page component being rendered rather than taking bindings
 * (ng syncs bindings both ways, and a read-only trail has nothing to push back).
 */
public class WCBreadcrumbs extends WCComponent {

	public Crumb crumb;

	public WCBreadcrumbs( NGContext context ) {
		super( context );
	}

	public List<Crumb> crumbs() {
		final WCComponent page = page();
		return page == null ? List.of( HOME_CRUMB ) : page.breadcrumbs();
	}

	public String leaf() {
		final WCComponent page = page();
		return page == null ? "" : page.breadcrumbLeaf();
	}
}
