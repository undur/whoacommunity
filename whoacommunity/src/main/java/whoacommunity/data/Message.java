package whoacommunity.data;

import jambalaya.interfaces.DateTimeStamped;
import jambalaya.interfaces.UUIDStamped;
import ng.appserver.privates.NGHTMLUtilities;
import whoacommunity.data.auto._Message;

public class Message extends _Message implements DateTimeStamped, UUIDStamped {

	/**
	 * @return The text for HTML display
	 *
	 * FIXME: Somewhat temporary until we've parsed the actual rich message content
	 */
	public String textRendered() {
		String text = text();

		text = NGHTMLUtilities.escapeHTML( text );
		text = text.replace( "\n", "<br>\n" );

		return text;
	}
}