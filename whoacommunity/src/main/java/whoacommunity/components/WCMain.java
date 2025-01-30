package whoacommunity.components;

import java.util.Collections;
import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGActionResults;
import ng.appserver.NGComponent;
import ng.appserver.NGContext;
import whoacommunity.app.Session;
import whoacommunity.app.WCCore;
import whoacommunity.data.Channel;
import whoacommunity.data.Message;

public class WCMain extends NGComponent {

	public String password;

	public Channel currentChannel;
	public Channel selectedChannel;
	public Message currentMessage;

	public WCMain( NGContext context ) {
		super( context );
	}

	public NGActionResults login() {
		if( "wocomponent".equals( password ) ) {
			((Session)session()).isLoggedIn = true;
		}

		return null;
	}

	public List<Channel> channels() {
		return ObjectSelect
				.query( Channel.class )
				.orderBy( Channel.NAME.ascInsensitive() )
				.select( WCCore.newContext() );
	}

	public NGActionResults selectChannel() {
		selectedChannel = currentChannel;
		return null;
	}

	/**
	 * FIXME: Still considering if we actually want formatting
	 */
	public String currentMessageDate() {
		return currentMessage.dateTime().toString();
	}

	public List<Message> messages() {

		if( selectedChannel == null ) {
			return Collections.emptyList();
		}

		final List<Message> messages = ObjectSelect
				.query( Message.class )
				.where( Message.CHANNEL.eq( selectedChannel ) )
				.orderBy( Message.DATE_TIME.asc() )
				.prefetch( Message.USER.joint() )
				.select( WCCore.newContext() );

		return filterMessages( messages );
	}

	/**
	 * @return The cleaned up message list
	 */
	public static List<Message> filterMessages( final List<Message> messages ) {
		return messages
				.stream()
				.filter( f -> !"channel_join".equals( f.slackSubtype() ) )
				.toList();
	}
}