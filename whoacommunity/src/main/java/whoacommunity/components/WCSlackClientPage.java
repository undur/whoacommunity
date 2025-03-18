package whoacommunity.components;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.Reaction;
import com.slack.api.model.User;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.app.Session;
import whoacommunity.slack.api.SlackAPIClient;

public class WCSlackClientPage extends NGComponent {

	private static final Logger logger = LoggerFactory.getLogger( WCSlackClientPage.class );

	private Map<String, Emoji> _emojiNamesToCodes;

	public String password;

	/**
	 * ID of the #general channel
	 */
	private String selectedConversationID = "C1LJMP8LW";

	public Conversation conversation;
	public Message message;
	public Reaction currentReaction;

	private SlackAPIClient slackClient = new SlackAPIClient( slackToken() );

	public WCSlackClientPage( NGContext context ) {
		super( context );
	}

	/**
	 * @return The jdbcURL property
	 */
	private static String slackToken() {
		return System.getProperty( "wc.slacktoken" );
	}

	public NGActionResults login() {
		if( password != null && "wocomponent".equals( password.toLowerCase() ) ) {
			((Session)session()).isLoggedIn = true;
			logger.info( "WOCOMMUNITY LOGIN SUCCEEDED" );
		}
		else {
			logger.info( "WOCOMMUNITY LOGIN FAILED" );
		}

		return null;
	}

	public NGActionResults selectChannel() {
		selectedConversationID = conversation.getId();
		return null;
	}

	public List<Message> messages() {
		return slackClient.fetchHistory( selectedConversationID );
	}

	public List<Conversation> conversations() {
		return slackClient.conversations();
	}

	public User user() {
		return slackClient.user( message.getUser() );
	}

	public LocalDateTime currentTS() {
		final String ts = message.getTs();
		final int dotIndex = ts.indexOf( "." );
		long value = Long.parseLong( ts.substring( 0, dotIndex ) );
		return Instant.ofEpochSecond( value ).atZone( ZoneId.systemDefault() ).toLocalDateTime();
	}

	public String currentReactionTitle() {
		final List<String> usernames = new ArrayList<>();

		for( final String userID : currentReaction.getUsers() ) {
			usernames.add( slackClient.user( userID ).getRealName() );
		}

		return String.join( "\n", usernames );
	}

	public String currentReactionEmojiURL() {
		if( _emojiNamesToCodes == null ) {
			try( final InputStream is = getClass().getClassLoader().getResourceAsStream( "ng/app/app-resources/better-emojis.json" )) {
				final String jsonString = new String( is.readAllBytes(), StandardCharsets.UTF_8 );

				final List<Emoji> fromJson = new GsonBuilder().create().fromJson( jsonString, TypeToken.getParameterized( ArrayList.class, Emoji.class ).getType() );

				_emojiNamesToCodes = new HashMap<>();

				for( Emoji emoji : fromJson ) {
					_emojiNamesToCodes.put( emoji.name, emoji );
				}
			}
			catch( IOException e ) {
				throw new UncheckedIOException( e );
			}
		}

		Emoji emoji = _emojiNamesToCodes.get( currentReaction.getName() );

		if( emoji == null ) {
			return "[emoji not found]";
		}

		return emoji.emojiAsString();
	}

	/**
	 * Wraps the emojis as read using Ian White's JSON-file from the answer provided here
	 *
	 *  https://stackoverflow.com/questions/39490865/how-can-i-get-the-full-list-of-slack-emoji-through-api
	 */
	public static class Emoji {
		public String name;
		public String unicode;
		public String id;
		public List<String> keywords;

		/**
		 * @return The emoji's stored unicode codepoints as a String containing the emoji
		 */
		public String emojiAsString() {
			final String[] codePoints = unicode.split( "-" );

			final StringBuilder sb = new StringBuilder();

			for( String nextInt : codePoints ) {
				sb.append( Character.toChars( Integer.decode( "0x" + nextInt ) ) );
			}

			return sb.toString();
		}
	}
}