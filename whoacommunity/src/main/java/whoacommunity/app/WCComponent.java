package whoacommunity.app;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;

public abstract class WCComponent extends NGComponent {

	public WCComponent( NGContext context ) {
		super( context );
	}

	/**
	 * FIXME: A hack to determine whether we show the admin page. Will eventually be controlled through login/access privileges // Hugi 2025-07-06
	 */
	@Deprecated
	public boolean isLocal() {
		return application().isDevelopmentMode();
	}

	public boolean showAdminStuff() {
		return isLocal();
	}
}