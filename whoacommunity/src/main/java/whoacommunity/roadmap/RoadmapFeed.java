package whoacommunity.roadmap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import whoacommunity.app.WCCore;
import whoacommunity.github.GithubGraphQLClient;
import whoacommunity.util.CachedFeed;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * The stack's roadmap: the items of the GitHub Project that organizes every open issue across the undur and
 * ngobjects repositories, with the fields set on the board (status, kind, theme, effort). Fetched over GraphQL
 * with the site's GitHub token, which needs the {@code read:project} scope; cached and refreshed like the
 * other feeds. Items whose status is internal to triage ("Proposed close", "Inbox", "Done") stay off the page.
 */
public class RoadmapFeed {

	/**
	 * The project's node id; set with wc.roadmapProjectId to point at another board
	 */
	private static final String DEFAULT_PROJECT_ID = "PVT_kwDOBFdw-84BleIP";

	public static final String PROJECT_URL = "https://github.com/orgs/undur/projects/4";

	public static final RoadmapFeed shared = new RoadmapFeed( Duration.ofHours( 1 ) );

	private final CachedFeed<List<Item>> _feed;

	public RoadmapFeed( final Duration cacheDuration ) {
		_feed = new CachedFeed<>( cacheDuration, RoadmapFeed::fetch, List.of() );
	}

	/**
	 * One issue on the board. {@code repo} is our Repo when the repository is one we track (for the emoji), else null.
	 */
	public record Item( Repo repo, String repoName, int number, String title, String url, String status, String kind, String theme, String effort, String twin ) {

		public String emoji() {
			return repo == null ? "" : repo.emoji();
		}

		public boolean hasTwin() {
			return twin != null && !twin.isBlank();
		}
	}

	public List<Item> items() {
		return _feed.value();
	}

	public boolean isLoaded() {
		return !items().isEmpty();
	}

	public List<Item> withStatus( final String status ) {
		return items().stream().filter( i -> status.equals( i.status() ) ).toList();
	}

	private static String projectId() {
		final String configured = System.getProperty( "wc.roadmapProjectId" );
		return configured == null || configured.isBlank() ? DEFAULT_PROJECT_ID : configured;
	}

	private static List<Item> fetch() {
		final String token = WCCore.githubToken();

		if( token == null || token.isBlank() ) {
			System.err.println( "RoadmapFeed: wc.githubToken not set, skipping refresh" );
			throw new IllegalStateException( "no token" );
		}

		try {
			final GithubGraphQLClient client = new GithubGraphQLClient( token );
			final List<Item> out = new ArrayList<>();
			String cursor = null;

			while( true ) {
				final JsonObject data = client.query( query( cursor ) );
				final JsonObject items = data.getAsJsonObject( "node" ).getAsJsonObject( "items" );

				for( final JsonElement e : items.getAsJsonArray( "nodes" ) ) {
					final Item item = toItem( e.getAsJsonObject() );

					if( item != null ) {
						out.add( item );
					}
				}

				final JsonObject pageInfo = items.getAsJsonObject( "pageInfo" );

				if( !pageInfo.get( "hasNextPage" ).getAsBoolean() ) {
					break;
				}

				cursor = pageInfo.get( "endCursor" ).getAsString();
			}

			return List.copyOf( out );
		}
		catch( final Exception e ) {
			System.err.println( "RoadmapFeed: refresh failed, keeping the previous value: " + e );
			throw new RuntimeException( e );
		}
	}

	private static String query( final String cursor ) {
		final String after = cursor == null ? "" : ", after: \"" + cursor + "\"";
		return """
				{
				  node(id: "%s") {
				    ... on ProjectV2 {
				      items(first: 100%s) {
				        pageInfo { hasNextPage endCursor }
				        nodes {
				          content { ... on Issue { number title url state repository { name owner { login } } } }
				          fieldValues(first: 12) {
				            nodes {
				              ... on ProjectV2ItemFieldSingleSelectValue { name field { ... on ProjectV2FieldCommon { name } } }
				              ... on ProjectV2ItemFieldTextValue { text field { ... on ProjectV2FieldCommon { name } } }
				            }
				          }
				        }
				      }
				    }
				  }
				}
				""".formatted( projectId(), after );
	}

	/**
	 * @return The item, or null for anything that isn't an open issue with a public-facing status
	 */
	private static Item toItem( final JsonObject node ) {
		final JsonElement contentElement = node.get( "content" );

		if( contentElement == null || contentElement.isJsonNull() || !contentElement.getAsJsonObject().has( "number" ) ) {
			return null;
		}

		final JsonObject content = contentElement.getAsJsonObject();

		if( !"OPEN".equals( string( content, "state" ) ) ) {
			return null;
		}

		String status = null, kind = null, theme = null, effort = null, twin = null;
		final JsonArray values = node.getAsJsonObject( "fieldValues" ).getAsJsonArray( "nodes" );

		for( final JsonElement v : values ) {
			final JsonObject value = v.getAsJsonObject();

			if( !value.has( "field" ) ) {
				continue;
			}

			final String field = string( value.getAsJsonObject( "field" ), "name" );
			final String text = value.has( "name" ) ? string( value, "name" ) : string( value, "text" );

			switch( field == null ? "" : field ) {
				case "Status" -> status = text;
				case "Kind" -> kind = text;
				case "Theme" -> theme = text;
				case "Effort" -> effort = text;
				case "Twin" -> twin = text;
				default -> {}
			}
		}

		if( status == null || status.equals( "Proposed close" ) || status.equals( "Inbox" ) || status.equals( "Done" ) ) {
			return null;
		}

		final JsonObject repository = content.getAsJsonObject( "repository" );
		final String repoName = string( repository, "name" );
		final Repo repo = Repos.repos().stream().filter( r -> r.githubRepoName().equals( repoName ) ).findFirst().orElse( null );

		return new Item( repo, repo == null ? repoName : repo.name(), content.get( "number" ).getAsInt(), string( content, "title" ), string( content, "url" ), status, kind, theme, effort, twin );
	}

	private static String string( final JsonObject object, final String key ) {
		final JsonElement e = object.get( key );
		return e == null || e.isJsonNull() ? null : e.getAsString();
	}
}
