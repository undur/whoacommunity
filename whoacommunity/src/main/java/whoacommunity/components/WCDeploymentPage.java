package whoacommunity.components;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;

public class WCDeploymentPage extends NGComponent {

	public WCDeploymentPage( NGContext context ) {
		super( context );
	}

	public String configString() {
		return """
				This is the configuration.
				""";
	}
}