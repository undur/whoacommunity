package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.ScriptDoc;

/**
 * The hybrid deployment guide: Apache keeps ports 80/443 (TLS via certbot,
 * vhosts, static files), modulo replaces mod_WebObjects as the WO-aware
 * reverse proxy behind it. Rendered from the actual comparison script —
 * see {@link ScriptDoc}.
 */
public class WCDeploymentApacheModuloPage extends WCComponent {

	@Override
	public String pageIdentifier() {
		return "deployment";
	}

	@Override
	public String currentDeploymentGuide() {
		return "apache-modulo";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "deployment", "/deployment" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "apache-modulo";
	}

	private static final ScriptDoc DOC = ScriptDoc.parse( "/setup-server-apache-modulo.sh" );

	public ScriptDoc.Step currentStep;
	public String currentParagraph;

	public WCDeploymentApacheModuloPage( NGContext context ) {
		super( context );
	}

	public List<ScriptDoc.Step> steps() {
		return DOC.steps();
	}

	public String fullScript() {
		return DOC.fullScript();
	}
}
