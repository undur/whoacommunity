package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * Server push in WebObjects applications with wo-adaptor-jetty: server-sent events and WebSockets.
 */
public class WCGuideServerPushPage extends WCComponent {

	public WCGuideServerPushPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "guides";
	}

	@Override
	public String pageTitle() {
		return "Server-sent events and WebSockets in WebObjects with wo-adaptor-jetty";
	}

	@Override
	public String pageDescription() {
		return "Pushing data from a WebObjects application to the browser with wo-adaptor-jetty: server-sent events with SSEStream and SSEHub, broadcasting to every connected client, WebSocket endpoints, the configuration a long-lived response needs, and the client-side traps worth knowing before you start.";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "guides", "/guides" ), new Crumb( "development", "/guides" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return "server push";
	}
}
