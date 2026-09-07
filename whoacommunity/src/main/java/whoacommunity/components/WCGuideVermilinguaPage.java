package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Building WebObjects applications and frameworks with the vermilingua Maven plugin.
 * Static content in the shell; the vermilingua project itself is the source of truth for details.
 */
public class WCGuideVermilinguaPage extends WCComponent {

	public WCGuideVermilinguaPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Building WebObjects applications with Maven and vermilingua";
	}

	@Override
	public String pageDescription() {
		return "How to build a WebObjects application or framework with the vermilingua Maven plugin: the pom, the project layout, build.properties, the self-contained .woa bundle, launch configuration, deployment, and migrating from wolifecycle.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "building", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "vermilingua";
	}
}
