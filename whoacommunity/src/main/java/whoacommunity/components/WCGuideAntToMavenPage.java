package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Moving a WOLips Ant project with the "Fluffy Bunny" layout to a Maven build with vermilingua, keeping the layout or
 * moving to the standard one, with ERProjectLayout so development doesn't depend on Eclipse natures.
 */
public class WCGuideAntToMavenPage extends WCComponent {

	public WCGuideAntToMavenPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Moving a WOLips Ant project to Maven";
	}

	@Override
	public String pageDescription() {
		return "Move a WOLips \"Fluffy Bunny\" Ant project to a Maven build with vermilingua: remove the Ant build, add a pom, rewrite build.properties, register ERProjectLayout, and optionally move to the standard Maven layout. Checked against Wonder 7.4 and 7.5-SNAPSHOT.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "building", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "ant to maven";
	}
}
