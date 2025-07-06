package whoacommunity.components;

import java.util.Collections;
import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.app.Session;
import whoacommunity.app.WCCore;
import whoacommunity.data.SlackChannel;
import whoacommunity.data.SlackMessage;

public class WCSlackArchivePage extends NGComponent {

	public String password;

	public SlackChannel currentChannel;
	public SlackChannel selectedChannel;
	public SlackMessage currentMessage;

	public WCSlackArchivePage( NGContext context ) {
		super( context );
	}

	public NGActionResults login() {
		if( "wocomponent".equals( password ) ) {
			((Session)session()).isLoggedIn = true;
		}

		return null;
	}

	public List<SlackChannel> channels() {
		return ObjectSelect
				.query( SlackChannel.class )
				.orderBy( SlackChannel.NAME.ascInsensitive() )
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

	public List<SlackMessage> messages() {

		if( selectedChannel == null ) {
			return Collections.emptyList();
		}

		final List<SlackMessage> messages = ObjectSelect
				.query( SlackMessage.class )
				.where( SlackMessage.CHANNEL.eq( selectedChannel ) )
				.orderBy( SlackMessage.DATE_TIME.asc() )
				.prefetch( SlackMessage.USER.joint() )
				.select( WCCore.newContext() );

		return filterMessages( messages );
	}

	/**
	 * @return The cleaned up message list
	 */
	public static List<SlackMessage> filterMessages( final List<SlackMessage> messages ) {
		return messages
				.stream()
				.filter( f -> !"channel_join".equals( f.slackSubtype() ) )
				.toList();
	}
}