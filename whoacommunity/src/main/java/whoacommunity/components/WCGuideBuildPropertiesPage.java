package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * build.properties as it should look today: what belongs in it, the current keys and who reads each, and every legacy key
 * with what replaced it.
 */
public class WCGuideBuildPropertiesPage extends WCComponent {

	public WCGuideBuildPropertiesPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "build.properties in WebObjects projects: the modern file and the legacy keys";
	}

	@Override
	public String pageDescription() {
		return "What belongs in a WebObjects project's build.properties today: the current keys, what each means and which tool reads it, and every legacy key from WOLips, wolifecycle and Project Wonder's builds, with what replaced it.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "building", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "build.properties";
	}
}
