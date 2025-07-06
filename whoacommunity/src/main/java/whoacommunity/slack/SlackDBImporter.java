package whoacommunity.slack;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import whoacommunity.app.WCCore;
import whoacommunity.data.Channel;
import whoacommunity.data.Message;
import whoacommunity.data.User;
import whoacommunity.slack.SlackImporter.ImportedSlackChannel;
import whoacommunity.slack.SlackImporter.SlackImportResult;
import whoacommunity.slack.SlackImporter.ImportedSlackMessage;
import whoacommunity.slack.SlackImporter.ImportedSlackUser;

public class SlackDBImporter {

	public static void main( String[] args ) {
		final ObjectContext oc = WCCore.newContext();

		final SlackImportResult importRun = SlackImporter.defaultImporter().run();

		for( ImportedSlackUser slackUser : importRun.users() ) {
			final User user = oc.newObject( User.class );
			user.setSlackID( slackUser.id() );
			user.setSlackUsername( slackUser.name() );
			user.setName( slackUser.real_name() );
			user.setEmailAddress( slackUser.profile().email() );
			user.setSlackProfileImageUrl( slackUser.profile().image_original() );
		}

		final Map<String, User> userCache = createUserCache( oc );

		for( ImportedSlackChannel slackChannel : importRun.channels() ) {
			Channel channel = oc.newObject( Channel.class );
			channel.setName( slackChannel.name() );
			channel.setSlackID( slackChannel.id() );

			for( ImportedSlackMessage slackMessage : slackChannel.messages() ) {
				Message message = oc.newObject( Message.class );
				message.setDateTime( parseTimestampString( slackMessage.ts() ) );
				message.setText( slackMessage.text() );
				message.setSlackType( slackMessage.type() );
				message.setSlackSubtype( slackMessage.subtype() );
				message.setChannel( channel );
				message.setUser( userCache.get( slackMessage.user() ) );
			}
		}

		//		oc.commitChanges();
	}

	private static LocalDateTime parseTimestampString( final String ts ) {
		int pointIndex = ts.indexOf( '.' );
		final long timestamp = Long.parseLong( ts.substring( 0, pointIndex ) );
		return LocalDateTime.ofInstant( Instant.ofEpochSecond( timestamp ), ZoneId.systemDefault() );
	}

	/**
	 * @return A cache of slack user IDs mapped to user objects
	 */
	private static Map<String, User> createUserCache( ObjectContext oc ) {
		final Map<String, User> map = new HashMap<>();

		ObjectSelect
				.query( User.class )
				.iterate( oc, user -> map.put( user.slackID(), user ) );

		return map;
	}
}