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
		return "Why a Maven-built WebObjects application should be launched from Eclipse as a Java Application rather than with WOLips' WOApplication launcher, the two settings that make it work, and what the WOApplication launcher gets wrong: test scope on the classpath, exclusions ignored, two versions of a library, and a debugger that reads the wrong one.";
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
