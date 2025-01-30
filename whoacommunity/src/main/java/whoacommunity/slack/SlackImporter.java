package whoacommunity.slack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Imports a slack workspace export, generating an in-memory object repository of the channels, users and messages.
 * This class does not create any type of relationships between the imported objects, that's the role of SlackDBImporter which will relate objects based on the IDs of the imported objects.
 */

public class SlackImporter {

	private Path _root;

	/**
	 *
	 * @param root The path to the folder containing the workspace export
	 */
	public SlackImporter( final Path root ) {
		Objects.requireNonNull( root );
		_root = root;
	}

	/**
	 * @return Path of the Slack export's root folder
	 */
	private Path root() {
		return _root;
	}

	/**
	 * @return A GSON instance for parsing the export data
	 */
	private static Gson gson() {
		return new GsonBuilder().create(); // I could spend 10 seconds caching this but I think I'd rather spend 20 seconds writing this comment
	}

	/**
	 * Stores the result of our import
	 */
	public record SlackImportResult( List<Channel> channels, List<User> users ) {}

	/**
	 * @param id ID of the channel
	 * @param name Name of the channel
	 */
	public record Channel( String id, String name, List<Message> messages ) {

		/**
		 * Since the imported channel won't contain any messages, we override the constructor to set messages to an emty ArrayList (that we can then add messages to)
		 */
		public Channel( String id, String name, List<Message> messages ) {
			this.id = id;
			this.name = name;
			this.messages = new ArrayList<>();
		}
	}

	/**
	 * @param user ID of the user that sent the message
	 * @param type Type of the event, can be one of 'message',... [something something]
	 * @param subtype Subtype of the event
	 * @param ts Timestamp of the message
	 * @param text Text of the message
	 *
	 * FIXME: Look into importing the rich text version // Hugi 2025-01-29
	 */
	public record Message( String user, String type, String subtype, String ts, String text ) {}

	/**
	 * @param id The users's id
	 * @param username of the user
	 * @param real_name The user's actual name
	 */
	public record User( String id, String name, String real_name, UserProfile profile ) {}

	/**
	 * Some more data about the user, including e-mail address, profile image etc.
	 *
	 * @param image_original Full size original profile image
	 * @param email Email address
	 */
	public record UserProfile( String image_original, String email ) {}

	public SlackImportResult run() {
		final List<Channel> channels = importChannelsAndMessages();
		final List<User> users = importUsers();

		return new SlackImportResult( channels, users );
	}

	private List<User> importUsers() {
		final Path path = root().resolve( "users.json" );
		return importList( path, User.class );
	}

	private List<Channel> importChannelsAndMessages() {
		final Path path = root().resolve( "channels.json" );

		final List<Channel> channels = importList( path, Channel.class );

		// Now add the channel's messages
		for( Channel channel : channels ) {
			channel.messages().addAll( importChannelMessages( channel.name() ) );
		}

		return channels;
	}

	private List<Message> importChannelMessages( String name ) {
		final Path channelFolderPath = root().resolve( name );

		final List<Message> messages = new ArrayList<>();

		try {
			Files.list( channelFolderPath ).forEach( messagePath -> {
				messages.addAll( importList( messagePath, Message.class ) );
			} );
		}
		catch( IOException e ) {
			throw new UncheckedIOException( e );
		}

		return messages;
	}

	private static <E> List<E> importList( final Path path, final Class<?> recordClass ) {
		try {
			final String jsonString = Files.readString( path );
			return List.of( gson().fromJson( jsonString, TypeToken.getArray( recordClass ).getType() ) );
		}
		catch( IOException e ) {
			throw new UncheckedIOException( e );
		}
	}

	/**
	 * Shortcut for creating an importer, since I'm a lazy bastard
	 */
	public static SlackImporter defaultImporter() {
		return new SlackImporter( Path.of( "/Users/hugi/tmp/wocommunity-slack-export-2025-01-28" ) );
	}

	public static void main( String[] args ) {
		final SlackImporter imp = defaultImporter();

		for( User user : imp.importUsers() ) {
			System.out.println( user.profile().email() );
		}

		for( Channel channel : imp.run().channels() ) {
			System.out.println( "======== " + channel.name() );
			for( Message message : channel.messages() ) {
				System.out.println( message.text() );
			}
		}
	}
}