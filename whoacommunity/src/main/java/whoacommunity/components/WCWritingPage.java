package whoacommunity.components;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The writing archive: every article, newest first.
 */
public class WCWritingPage extends WCComponent {

	public WCWritingPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "writing";
	}

	@Override
	public String breadcrumbLeaf() {
		return "writing";
	}
}
