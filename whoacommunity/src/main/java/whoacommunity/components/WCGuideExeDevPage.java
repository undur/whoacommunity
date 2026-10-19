package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Deploying to exe.dev with vermilingua:ssh-deploy — one Maven command from a project to a running application behind HTTPS.
 */
public class WCGuideExeDevPage extends WCComponent {

	public WCGuideExeDevPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Deploying on exe.dev with vermilingua:ssh-deploy";
	}

	@Override
	public String pageDescription() {
		return "Put a WebObjects or ng-objects application on the internet with one Maven command: vermilingua:ssh-deploy creates an exe.dev VM, uploads the .woa, installs Java and runs the application as a service behind exe.dev's HTTPS. Setup, configuration, redeploying, rollback and troubleshooting.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "deployment", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "vermilingua:ssh-deploy";
	}
}
