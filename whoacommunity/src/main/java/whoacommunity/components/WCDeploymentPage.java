package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.ScriptDoc;

/**
 * The full-stack setup guide for the pure modulo deployment, rendered from
 * the actual setup-server.sh — see {@link ScriptDoc} for the literate
 * rendering. Because the explanation column is unselectable, selecting and
 * copying the page yields a runnable script (the copy button hands over the
 * verbatim file, comments included).
 */
public class WCDeploymentPage extends WCComponent {

	@Override
	public String pageIdentifier() {
		return "deployment";
	}

	@Override
	public String currentDeploymentGuide() {
		return "modulo";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "deployment", "/deployment" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "modulo";
	}

	private static final ScriptDoc DOC = ScriptDoc.parse( "/setup-server.sh" );

	public ScriptDoc.Step currentStep;
	public String currentParagraph;

	public WCDeploymentPage( NGContext context ) {
		super( context );
	}

	public List<ScriptDoc.Step> steps() {
		return DOC.steps();
	}

	/** The verbatim script, for the copy button. */
	public String fullScript() {
		return DOC.fullScript();
	}
}
