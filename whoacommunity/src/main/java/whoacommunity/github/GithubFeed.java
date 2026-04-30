package whoacommunity.github;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import whoacommunity.app.WCCore;
import whoacommunity.github.GithubResponse.CommitNode;
import whoacommunity.github.GithubResponse.IssueNode;
import whoacommunity.github.GithubResponse.ReleaseNode;
import whoacommunity.github.GithubResponse.RepoNode;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * Fetches open issues, published releases, and recent commits across all
 * repos flagged with {@code includeInGithubFeed} via a single GitHub
 * GraphQL request, and caches the result for a configurable duration.
 */
public class GithubFeed {

	/**
	 * How many commits per repo to ask GitHub for. The full set is sliced
	 * down to fewer items for the sidebar; the dev-feed page sees the lot.
	 */
	private static final int COMMITS_PER_REPO = 50;

	/**
	 * Singleton instance, refreshed at most once per its cache duration.
	 */
	public static final GithubFeed shared = new GithubFeed( Duration.ofMinutes( 5 ) );

	/**
	 * Reusable Gson with an Instant adapter — GitHub returns ISO-8601
	 * timestamps everywhere we read a time field.
	 */
	private static final Gson GSON = buildGson();

	private final Duration _cacheDuration;
	private Instant _lastRefreshed;
	private List<OpenIssue> _issues = List.of();
	private List<Release> _releases = List.of();
	private List<Commit> _commits = List.of();

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

	public List<Commit> commits() {
		ensureFresh();
		return _commits;
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
			_commits = List.of();
			return;
		}

		final GithubGraphQLClient client = new GithubGraphQLClient( token );
		final String query = buildQuery( tracked );
		final JsonObject data = client.query( query );

		final List<RepoNode> repoNodes = deserializeRepoNodes( data, tracked.size() );

		_issues = collectIssues( tracked, repoNodes );
		_releases = collectReleases( tracked, repoNodes );
		_commits = collectCommits( tracked, repoNodes );
	}

	/**
	 * Build a single GraphQL query that aliases each repo by its index, so we
	 * can map response fields back to the tracked repo list.
	 */
	private static String buildQuery( final List<Repo> repos ) {
		final String repoFragmentTemplate = """
				r%d: repository(owner: "%s", name: "%s") {
					issues(states: OPEN, first: 5, orderBy: {field: UPDATED_AT, direction: DESC}) {
						nodes { number title url updatedAt author { login } }
					}
					releases(first: 3, orderBy: {field: CREATED_AT, direction: DESC}) {
						nodes { name tagName url createdAt isPrerelease isDraft }
					}
					defaultBranchRef {
						target {
							... on Commit {
								history(first: %d) {
									nodes {
										messageHeadline
										committedDate
										url
										author { user { login } name }
									}
								}
							}
						}
					}
				}
				""";

		final StringBuilder repoFragments = new StringBuilder();
		for( int i = 0; i < repos.size(); i++ ) {
			final Repo repo = repos.get( i );
			repoFragments.append( repoFragmentTemplate.formatted(
					i,
					escape( repo.githubOwner() ),
					escape( repo.githubRepoName() ),
					COMMITS_PER_REPO ) );
		}

		return """
				{
				%s}
				""".formatted( repoFragments );
	}

	/**
	 * Parallel list to {@code tracked}: for each repo at index i, the parsed
	 * "r{i}" node, or null if the API didn't return one for that repo.
	 */
	private static List<RepoNode> deserializeRepoNodes( final JsonObject data, final int repoCount ) {
		final List<RepoNode> out = new ArrayList<>( repoCount );
		for( int i = 0; i < repoCount; i++ ) {
			final JsonElement el = data.get( "r" + i );
			out.add( ( el == null || el.isJsonNull() ) ? null : GSON.fromJson( el, RepoNode.class ) );
		}
		return out;
	}

	private static List<OpenIssue> collectIssues( final List<Repo> repos, final List<RepoNode> repoNodes ) {
		final List<OpenIssue> out = new ArrayList<>();
		for( int i = 0; i < repos.size(); i++ ) {
			final RepoNode rn = repoNodes.get( i );
			if( rn == null || rn.issues() == null || rn.issues().nodes() == null ) continue;

			for( IssueNode n : rn.issues().nodes() ) {
				out.add( new OpenIssue(
						repos.get( i ),
						n.number(),
						n.title(),
						n.url(),
						n.updatedAt(),
						n.author() == null ? null : n.author().login() ) );
			}
		}
		out.sort( Comparator.comparing( OpenIssue::updatedAt, Comparator.nullsLast( Comparator.reverseOrder() ) ) );
		return List.copyOf( out );
	}

	private static List<Release> collectReleases( final List<Repo> repos, final List<RepoNode> repoNodes ) {
		final List<Release> out = new ArrayList<>();
		for( int i = 0; i < repos.size(); i++ ) {
			final RepoNode rn = repoNodes.get( i );
			if( rn == null || rn.releases() == null || rn.releases().nodes() == null ) continue;

			for( ReleaseNode n : rn.releases().nodes() ) {
				if( n.isDraft() || n.isPrerelease() ) continue;
				out.add( new Release(
						repos.get( i ),
						n.name(),
						n.tagName(),
						n.url(),
						n.createdAt() ) );
			}
		}
		out.sort( Comparator.comparing( Release::createdAt, Comparator.nullsLast( Comparator.reverseOrder() ) ) );
		return List.copyOf( out );
	}

	private static List<Commit> collectCommits( final List<Repo> repos, final List<RepoNode> repoNodes ) {
		final List<Commit> out = new ArrayList<>();
		for( int i = 0; i < repos.size(); i++ ) {
			final RepoNode rn = repoNodes.get( i );
			if( rn == null
					|| rn.defaultBranchRef() == null
					|| rn.defaultBranchRef().target() == null
					|| rn.defaultBranchRef().target().history() == null
					|| rn.defaultBranchRef().target().history().nodes() == null ) continue;

			for( CommitNode n : rn.defaultBranchRef().target().history().nodes() ) {
				out.add( new Commit(
						repos.get( i ),
						n.messageHeadline(),
						n.url(),
						commitAuthor( n ),
						n.committedDate() ) );
			}
		}
		out.sort( Comparator.comparing( Commit::committedAt, Comparator.nullsLast( Comparator.reverseOrder() ) ) );
		return List.copyOf( out );
	}

	private static String commitAuthor( final CommitNode n ) {
		if( n.author() == null ) return null;

		final String login = n.author().user() == null ? null : n.author().user().login();
		if( login != null && !login.isBlank() ) {
			return "hugithordarson".equals( login ) ? "hugi" : login;
		}

		// Fall back to the raw author name (covers commits where the author's
		// email isn't linked to a GitHub account)
		return n.author().name();
	}

	private static Gson buildGson() {
		final JsonDeserializer<Instant> instantAdapter = ( json, type, ctx ) -> {
			if( json == null || json.isJsonNull() ) return null;
			final String s = json.getAsString();
			if( s == null || s.isBlank() ) return null;
			try {
				return Instant.parse( s );
			}
			catch( Exception e ) {
				return null;
			}
		};
		return new GsonBuilder()
				.registerTypeAdapter( Instant.class, instantAdapter )
				.create();
	}

	private static String escape( final String s ) {
		return s.replace( "\\", "\\\\" ).replace( "\"", "\\\"" );
	}
}
