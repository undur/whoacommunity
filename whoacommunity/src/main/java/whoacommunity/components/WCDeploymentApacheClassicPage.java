package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.util.ScriptDoc;

/**
 * The classic deployment guide: Apache + compiled mod_WebObjects + certbot,
 * as it has been done since the last century. Published for comparison with
 * the pure stack. Rendered from the actual comparison script — see
 * {@link ScriptDoc}.
 */
public class WCDeploymentApacheClassicPage extends NGComponent {

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
