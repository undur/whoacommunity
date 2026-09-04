package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Setting up a machine for wonder-slim development: the JetBrains Runtime with
 * DCEVM, HotswapAgent, Eclipse and Parslips. Static content, in the shell.
 */
public class WCGuideWonderSlimDevPage extends WCComponent {

	public WCGuideWonderSlimDevPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "wonder-slim development";
	}
}
