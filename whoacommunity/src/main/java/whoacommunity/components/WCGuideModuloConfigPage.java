package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Reference for modulo's configuration: the root file, every table and field, sites, rewrites, what modulo keeps on
 * disk, and the command-line overrides.
 */
public class WCGuideModuloConfigPage extends WCComponent {

	public WCGuideModuloConfigPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Configuring modulo";
	}

	@Override
	public String pageDescription() {
		return "Every setting in modulo's configuration: the TOML root file, the frontend, admin, wotaskd and acme tables, sites and their hostnames, TLS, canonical redirects, rewrite rules, include files, what modulo stores on disk, and the command-line overrides.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "running", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "configuring modulo";
	}
}
