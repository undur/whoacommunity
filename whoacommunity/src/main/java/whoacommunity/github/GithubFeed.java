package whoacommunity.github;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import whoacommunity.app.WCCore;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * Fetches open issues and published releases across all repos flagged with
 * {@code includeInGithubFeed} via a single GitHub GraphQL request, and caches
 * the result for a configurable duration.
 */
public class GithubFeed {

	/**
	 * Singleton instance, refreshed at most once per its cache duration.
	 */
	public static final GithubFeed shared = new GithubFeed( Duration.ofMinutes( 5 ) );

	private final Duration _cacheDuration;
	private Instant _lastRefreshed;
	private List<OpenIssue> _issues = List.of();
	private List<Release> _releases = List.of();

	public GithubFeed( final Duration cacheDuration ) {
		_cacheDuration = cacheDuration;
	}

	public List<OpenIssue> issues() {
		ensureFresh();
		return _issues;
	}

	public List<Release> releases() {
		ensureFresh();
		return _releases;
	}

	private boolean shouldRefresh() {
		if( _lastRefreshed == null || _cacheDuration == null ) {
			return true;
		}
		return Duration.between( _lastRefreshed, Instant.now() ).compareTo( _cacheDuration ) > 0;
	}

	private synchronized void ensureFresh() {
		if( !shouldRefresh() ) {
			return;
		}

		try {
			refresh();
		}
		catch( Exception e ) {
			// Keep stale data on failure rather than blanking the UI
			System.err.println( "GithubFeed refresh failed: " + e.getMessage() );
			e.printStackTrace();
		}

		// Even on failure, mark refreshed so we don't hammer GitHub on every page load
		_lastRefreshed = Instant.now();
	}

	private void refresh() throws Exception {
		final String token = WCCore.githubToken();

		if( token == null || token.isBlank() ) {
			System.err.println( "GithubFeed: wc.githubToken not set, skipping refresh" );
			return;
		}

		final List<Repo> tracked = Repos.repos().stream()
				.filter( Repo::includeInGithubFeed )
				.toList();

		if( tracked.isEmpty() ) {
			_issues = List.of();
			_releases = List.of();
			return;
		}

		final GithubGraphQLClient client = new GithubGraphQLClient( token );
		final String query = buildQuery( tracked );
		final JsonObject data = client.query( query );

		_issues = parseIssues( data, tracked );
		_releases = parseReleases( data, tracked );
	}

	/**
	 * Build a single GraphQL query that aliases each repo by its index, so we
	 * can map response fields back to the tracked repo list.
	 */
	private static String buildQuery( final List<Repo> repos ) {
		final StringBuilder sb = new StringBuilder( "{ " );
		for( int i = 0; i < repos.size(); i++ ) {
			final Repo repo = repos.get( i );
			sb.append( "r" ).append( i ).append( ": repository(owner: \"" ).append( escape( repo.githubOwner() ) )
					.append( "\", name: \"" ).append( escape( repo.githubRepoName() ) ).append( "\") {" )
					.append( " issues(states: OPEN, first: 5, orderBy: {field: UPDATED_AT, direction: DESC}) {" )
					.append( " nodes { number title url updatedAt author { login } } }" )
					.append( " releases(first: 3, orderBy: {field: CREATED_AT, direction: DESC}) {" )
					.append( " nodes { name tagName url publishedAt isPrerelease isDraft } }" )
					.append( " } " );
		}
		sb.append( "}" );
		return sb.toString();
	}

	private static List<OpenIssue> parseIssues( final JsonObject data, final List<Repo> repos ) {
		final List<OpenIssue> out = new ArrayList<>();

		for( int i = 0; i < repos.size(); i++ ) {
			final Repo repo = repos.get( i );
			final JsonObject repoNode = optObject( data, "r" + i );
			if( repoNode == null ) continue;

			final JsonArray nodes = optArray( optObject( repoNode, "issues" ), "nodes" );
			if( nodes == null ) continue;

			for( JsonElement el : nodes ) {
				final JsonObject n = el.getAsJsonObject();
				out.add( new OpenIssue(
						repo,
						optInt( n, "number", 0 ),
						optString( n, "title", "" ),
						optString( n, "url", "" ),
						parseInstant( optString( n, "updatedAt", null ) ),
						authorLogin( n ) ) );
			}
		}

		out.sort( Comparator.comparing( OpenIssue::updatedAt, Comparator.nullsLast( Comparator.reverseOrder() ) ) );
		return List.copyOf( out );
	}

	private static List<Release> parseReleases( final JsonObject data, final List<Repo> repos ) {
		final List<Release> out = new ArrayList<>();

		for( int i = 0; i < repos.size(); i++ ) {
			final Repo repo = repos.get( i );
			final JsonObject repoNode = optObject( data, "r" + i );
			if( repoNode == null ) continue;

			final JsonArray nodes = optArray( optObject( repoNode, "releases" ), "nodes" );
			if( nodes == null ) continue;

			for( JsonElement el : nodes ) {
				final JsonObject n = el.getAsJsonObject();
				if( optBoolean( n, "isDraft", false ) || optBoolean( n, "isPrerelease", false ) ) {
					continue;
				}
				out.add( new Release(
						repo,
						optString( n, "name", null ),
						optString( n, "tagName", "" ),
						optString( n, "url", "" ),
						parseInstant( optString( n, "publishedAt", null ) ) ) );
			}
		}

		out.sort( Comparator.comparing( Release::publishedAt, Comparator.nullsLast( Comparator.reverseOrder() ) ) );
		return List.copyOf( out );
	}

	// ---- tiny JSON helpers (Gson getters return null/throw inconsistently, so wrap them) ----

	private static String authorLogin( final JsonObject issueNode ) {
		final JsonObject author = optObject( issueNode, "author" );
		return author == null ? null : optString( author, "login", null );
	}

	private static JsonObject optObject( final JsonObject parent, final String key ) {
		if( parent == null ) return null;
		final JsonElement el = parent.get( key );
		return ( el == null || el.isJsonNull() || !el.isJsonObject() ) ? null : el.getAsJsonObject();
	}

	private static JsonArray optArray( final JsonObject parent, final String key ) {
		if( parent == null ) return null;
		final JsonElement el = parent.get( key );
		return ( el == null || el.isJsonNull() || !el.isJsonArray() ) ? null : el.getAsJsonArray();
	}

	private static String optString( final JsonObject parent, final String key, final String fallback ) {
		final JsonElement el = parent.get( key );
		return ( el == null || el.isJsonNull() ) ? fallback : el.getAsString();
	}

	private static int optInt( final JsonObject parent, final String key, final int fallback ) {
		final JsonElement el = parent.get( key );
		return ( el == null || el.isJsonNull() ) ? fallback : el.getAsInt();
	}

	private static boolean optBoolean( final JsonObject parent, final String key, final boolean fallback ) {
		final JsonElement el = parent.get( key );
		return ( el == null || el.isJsonNull() ) ? fallback : el.getAsBoolean();
	}

	private static Instant parseInstant( final String iso ) {
		if( iso == null || iso.isBlank() ) return null;
		try {
			return Instant.parse( iso );
		}
		catch( Exception e ) {
			return null;
		}
	}

	private static String escape( final String s ) {
		return s.replace( "\\", "\\\\" ).replace( "\"", "\\\"" );
	}
}
