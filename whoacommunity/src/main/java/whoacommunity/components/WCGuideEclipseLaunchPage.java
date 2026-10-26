package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Launching a WebObjects application from Eclipse as a plain Java application rather than through WOLips' WOApplication
 * launch type, and why: the WOApplication launcher computes its classpath outside Maven.
 */
public class WCGuideEclipseLaunchPage extends WCComponent {

	public WCGuideEclipseLaunchPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Launching WebObjects applications from Eclipse as plain Java applications";
	}

	@Override
	public String pageDescription() {
		return "Launch a Maven-built WebObjects application from Eclipse as a Java Application, not with WOLips' WOApplication launcher: the one VM argument it takes, and why the WOApplication launcher gets the classpath wrong: test scope included, exclusions ignored, libraries twice, a debugger reading the wrong one.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "launching from eclipse";
	}
}
