package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The Parsley template syntax: inline dynamic tags, binding values, directives, bundle templates.
 * Written against Parsley 1.6.0 and its association factory; the project is the source of truth.
 */
public class WCGuideParsleyPage extends WCComponent {

	public WCGuideParsleyPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Parsley template syntax for WebObjects components";
	}

	@Override
	public String pageDescription() {
		return "A reference for writing WebObjects component templates with Parsley: inline wo: tags and shortcuts, $ bindings, constants, negation and parent bindings, OGNL, conditionals and repetitions, p:raw and p:comment, .wod declarations, tag aliases, and inline errors.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "parsley syntax";
	}
}
