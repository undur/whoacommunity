package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * URL routing in wonder-slim: RouteTable, handler shapes, parameters, and why the same URLs work in development and production.
 */
public class WCGuideRoutesPage extends WCComponent {

	public WCGuideRoutesPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "URL routing in WebObjects applications with wonder-slim";
	}

	@Override
	public String pageDescription() {
		return "A beginner's guide to wonder-slim's router: declaring routes, parameters and converters, links built from the routes, handlers, query parameters and forms, groups and filters, what answers when nothing does, and the same URLs in development and production.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "url routing";
	}
}
