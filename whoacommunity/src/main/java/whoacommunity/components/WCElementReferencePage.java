package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.elements.ElementReferenceFeed;
import whoacommunity.elements.ElementReferenceFeed.Binding;
import whoacommunity.elements.ElementReferenceFeed.Constraint;
import whoacommunity.elements.ElementReferenceFeed.Element;
import whoacommunity.elements.ElementReferenceFeed.Source;

/**
 * The element reference: every dynamic element of a library with its bindings, rendered from the JSON the
 * AjaxPlayground publishes (which it in turn renders from the libraries' .apiext files). One page per source,
 * WebObjects by default, the others as tabs.
 */
public class WCElementReferencePage extends WCComponent {

	public static final String DEFAULT_SLUG = "webobjects";

	/**
	 * The slug of the source to show, from the URL
	 */
	public String slug = DEFAULT_SLUG;

	public Source currentSource;
	public Element currentElement;
	public Binding currentBinding;
	public Constraint currentConstraint;
	public String currentTag;

	public WCElementReferencePage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "reference";
	}

	@Override
	public String pageTitle() {
		final Source source = source();
		return source == null ? "Element reference" : source.title() + " element reference";
	}

	@Override
	public String pageDescription() {
		final Source source = source();
		final String what = source == null ? "WebObjects, wonder-slim and AjaxSlim" : source.title();
		return "Every dynamic element of " + what + " with its bindings: types, direction, required bindings, defaults, constraints and deprecations, rendered from the libraries' .apiext files.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "element reference", "/element-reference" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		final Source source = source();
		return source == null ? slug : source.title();
	}

	public List<Source> sources() {
		return ElementReferenceFeed.shared.sources();
	}

	public Source source() {
		return ElementReferenceFeed.shared.sourceForSlug( slug );
	}

	public boolean hasSource() {
		return source() != null;
	}

	public boolean isLoaded() {
		return ElementReferenceFeed.shared.isLoaded();
	}

	public List<Element> elements() {
		final Source source = source();
		return source == null ? List.of() : source.elements();
	}

	public int elementCount() {
		return elements().size();
	}

	public String currentSourceURL() {
		return "/element-reference/" + currentSource.slug();
	}

	public String currentSourceClass() {
		return currentSource.slug().equals( slug ) ? "is-on" : "";
	}

	public String currentElementAnchor() {
		return "#" + currentElement.className();
	}

	/**
	 * @return The element on the playground, for the "on the playground" link: its page anchor there
	 */
	public String playgroundURL() {
		final Source source = source();
		return ElementReferenceFeed.baseURL() + ( source == null ? "/element-reference" : source.path() );
	}

	/**
	 * @return The AjaxSlim category label for the current tag; the playground's own editorial taxonomy
	 */
	public String currentTagLabel() {
		return switch( currentTag == null ? "" : currentTag ) {
			case "update" -> "Update";
			case "widget" -> "Widget";
			case "server" -> "Server";
			case "trigger" -> "Activity";
			default -> currentTag;
		};
	}

	public String currentTagClass() {
		return "elref-tag elref-tag-" + currentTag;
	}
}
