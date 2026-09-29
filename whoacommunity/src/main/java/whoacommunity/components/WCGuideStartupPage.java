package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * How a wonder-slim application starts: the configuration composed from its sources before the application exists,
 * framework plugins and the points at which they run, and what's in place at each step.
 */
public class WCGuideStartupPage extends WCComponent {

	public WCGuideStartupPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "How a WebObjects application starts with wonder-slim: configuration and plugins";
	}

	@Override
	public String pageDescription() {
		return "Startup in wonder-slim, step by step: the configuration composed from all its sources before the application is constructed, where each value comes from, framework plugins and the three points at which they run, and moving from ERXFrameworkPrincipal.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "startup";
	}
}
