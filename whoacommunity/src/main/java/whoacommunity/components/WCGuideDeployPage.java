package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Deploying a build with vermilingua:deploy — one Maven command from the build machine to bounced instances on every host.
 */
public class WCGuideDeployPage extends WCComponent {

	public WCGuideDeployPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Deploying WebObjects applications with vermilingua:deploy";
	}

	@Override
	public String pageDescription() {
		return "Ship a WebObjects application build with one Maven command: vermilingua:deploy packs the .woa, posts it to JavaMonitor, and wotaskd swaps the bundle into place and restarts the instances on every host. Configuration, CI use, what happens on the server, rollback and troubleshooting.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "deployment", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "vermilingua:deploy";
	}
}
