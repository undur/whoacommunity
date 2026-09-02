package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.ScriptDoc;

/**
 * The classic deployment guide: Apache + compiled mod_WebObjects + certbot,
 * as it has been done since the last century. Published for comparison with
 * the pure stack. Rendered from the actual comparison script — see
 * {@link ScriptDoc}.
 */
public class WCDeploymentApacheClassicPage extends WCComponent {

	@Override
	public String pageIdentifier() {
		return "deployment";
	}

	@Override
	public String currentDeploymentGuide() {
		return "apache-mod-webobjects";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "deployment", "/deployment" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "apache-mod-webobjects";
	}

	private static final ScriptDoc DOC = ScriptDoc.parse( "/setup-server-apache-mod_webobjects.sh" );

	public ScriptDoc.Step currentStep;
	public String currentParagraph;

	public WCDeploymentApacheClassicPage( NGContext context ) {
		super( context );
	}

	public List<ScriptDoc.Step> steps() {
		return DOC.steps();
	}

	public String fullScript() {
		return DOC.fullScript();
	}
}
