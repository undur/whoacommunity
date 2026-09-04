package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The guides page. Deployment is its first section: what the three deployment
 * guides share, and a card for each. More sections will follow.
 */
public class WCGuidesPage extends WCComponent {

	/**
	 * One of the three deployment guides
	 *
	 * @param id    Matches {@link WCComponent#currentDeploymentGuide()} on the guide's page
	 * @param tag   A short status pill: what role the guide plays
	 * @param blurb What sits at the edge in this variant, and why you'd choose it
	 */
	/**
	 * @param nodes The request path drawn in the card: edge → (proxy) → apps
	 */
	public record Guide( String id, String emoji, String title, String url, String tag, boolean beta, String blurb, List<Node> nodes ) {}

	/**
	 * A box in the card's little topology diagram
	 *
	 * @param role edge (owns 80/443 and TLS), proxy (routes to instances) or apps (the instances themselves)
	 */
	public record Node( String label, String caption, String role ) {}

	private static final Node APPS = new Node( "apps", "instances", "apps" );

	private static final List<Guide> GUIDES = List.of(
			new Guide( "modulo", "🤖", "Pure modulo", "/deployment-config", "new hotness", true,
					"modulo alone on ports 80 and 443: TLS from Let's Encrypt via native ACME, virtual hosts, static files and routing to instances, from one TOML file. No Apache, no certbot, no compiled module. The field-tested path, and the one we run ourselves.",
					List.of( new Node( "modulo", "80/443 · TLS · routing", "edge" ), APPS ) ),
			new Guide( "apache-modulo", "🪶", "Apache + modulo", "/deployment-apache-modulo", "hybrid", false,
					"Apache keeps the edge — certbot certificates, vhosts, static files — and modulo stands behind it as the WO-aware reverse proxy, replacing mod_WebObjects without compiling anything. For an Apache estate you can't, or won't, retire.",
					List.of( new Node( "Apache", "80/443 · TLS", "edge" ), new Node( "modulo", "routing", "proxy" ), APPS ) ),
			new Guide( "apache-mod-webobjects", "🏛️", "Apache + mod_WebObjects", "/deployment-apache-mod-webobjects", "classic", false,
					"Deployment as it has been done since the last century: Apache with a compiled mod_WebObjects module, certbot, and a C toolchain on the production server to build it. Published for comparison, so you can see what the other two leave out.",
					List.of( new Node( "Apache", "80/443 · TLS", "edge" ), new Node( "mod_WebObjects", "routing, in-process", "proxy" ), APPS ) ) );

	/**
	 * Guides about the development environment rather than the server. No topology row on these cards.
	 */
	private static final List<Guide> DEVELOPMENT_GUIDES = List.of(
			new Guide( "wonder-slim-development", "🛠️", "Setting up for wonder-slim development", "/guide/wonder-slim-development", "macOS", false,
					"A JDK that redefines classes while the application runs, Eclipse with Parslips, and Maven pointed at the WOCommunity repository. The JetBrains Runtime through SDKMAN!, HotswapAgent in one copy, and the VM arguments that make it all work.", List.of() ) );

	public List<Guide> developmentGuides() {
		return DEVELOPMENT_GUIDES;
	}

	public boolean currentGuideHasNodes() {
		return !currentGuide.nodes().isEmpty();
	}

	public Guide currentGuide;
	public Node currentNode;

	public WCGuidesPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String breadcrumbLeaf() {
		return "guides";
	}

	public String currentNodeClass() {
		return "topo-node topo-" + currentNode.role();
	}

	public List<Guide> guides() {
		return GUIDES;
	}

	/**
	 * @return The guides, for the site tree (KVC needs the instance method above)
	 */
	public static List<Guide> allGuides() {
		return GUIDES;
	}
}
