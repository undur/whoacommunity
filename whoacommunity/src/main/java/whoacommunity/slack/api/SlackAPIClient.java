package whoacommunity.slack.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.User;

public class SlackAPIClient {

	private static final Logger logger = LoggerFactory.getLogger( SlackAPIClient.class );

	/**
	 * Slack application token (set in the constructor)
	 */
	private final String _token;

	/**
	 * User cache. Stores user profiles, ensuring we only need to fetch them once.
	 */
	private Map<String, User> _cachedUsers = new ConcurrentHashMap<>();

	/**
	 * Emoji cache. Ensures we only fetch the emoji list once.
	 */
	private Map<String, String> _cachedEmojis = new ConcurrentHashMap<>();

	public SlackAPIClient( final String token ) {
		Objects.requireNonNull( token, "You must provide a slack Application token" );
		_token = token;
	}

	/**
	 * @return An instance of slack's API client
	 */
	private MethodsClient iClient() {
		return Slack.getInstance().methods();
	}

	/**
	 * @return Fetch conversation history using ID from last example
	 */
	public List<Message> fetchHistory( String channelID ) {
		try {
			return iClient().conversationsHistory( r -> r
					.token( _token )
					.limit( 1000 ) // 1000 messages seems to be the max we can fetch in a single API call
					.channel( channelID ) )
					.getMessages();
		}
		catch( IOException | SlackApiException e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * @return An alphabetically ordered list of conversations (which we humans usually call channels)
	 */
	public List<Conversation> conversations() {

		try {
			final ConversationsListResponse result = iClient().conversationsList( r -> r.token( _token ) );

			// Now let's sort this
			final List<Conversation> conversations = new ArrayList<>( result.getChannels() );
			Collections.sort( conversations, Comparator.comparing( Conversation::getName ) );
			return conversations;
		}
		catch( IOException | SlackApiException e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * @return User matching the given userID.
	 *
	 * Each userID is only fetched once, then cached indefinitely with the client
	 */
	public User user( String userID ) {
		return _cachedUsers.computeIfAbsent( userID, id -> {
			try {
				logger.info( "Fetching user with ID {}", id );
				return iClient().usersInfo( r -> r
						.token( _token )
						.user( userID ) )
						.getUser();
			}
			catch( IOException | SlackApiException e ) {
				throw new RuntimeException( e );
			}
		} );
	}

	/**
	 * @return An alphabetically ordered list of conversations (which we humans usually call channels)
	 */
	public Map<String, String> emojis() {

		if( _cachedEmojis == null ) {
			try {
				_cachedEmojis = iClient().emojiList( r -> r.token( _token ) ).getEmoji();
			}
			catch( IOException | SlackApiException e ) {
				throw new RuntimeException( e );
			}
		}

		return _cachedEmojis;
	}
}