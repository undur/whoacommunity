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
		return "Clean URLs for WebObjects applications with wonder-slim's route table: mapping paths to pages, lambdas and methods, reading path and query parameters, returning pages, data and redirects, and why the same URLs work in development and behind modulo or Apache in production.";
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
