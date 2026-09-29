package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Logging in wonder-slim: the backend modules, configuring levels and the layout in keys every backend understands, the
 * layers of configuration and which wins, changing levels on a running instance, and WebObjects' own output.
 */
public class WCGuideLoggingPage extends WCComponent {

	public WCGuideLoggingPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Configuring logging in WebObjects applications with wonder-slim";
	}

	@Override
	public String pageDescription() {
		return "Logging in wonder-slim: choosing reload4j or logback, setting levels and the layout in keys every backend understands, how those combine with log4j and logback configuration, changing levels on a running instance, and where WebObjects' own output goes.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "logging";
	}
}
