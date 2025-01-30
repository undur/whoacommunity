package whoacommunity.data;

import jambalaya.interfaces.DateTimeStamped;
import jambalaya.interfaces.UUIDStamped;
import whoacommunity.data.auto._Message;

public class Message extends _Message implements DateTimeStamped, UUIDStamped {

	public String textRendered() {
		return text().replace( "\n", "<br>\n" );
	}
}