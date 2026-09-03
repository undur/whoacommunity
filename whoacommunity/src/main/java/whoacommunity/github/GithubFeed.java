package whoacommunity.github;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import whoacommunity.util.CachedFeed;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * Fetches open issues, published releases, recent commits, READMEs and
 * descriptions across all repos flagged with {@code includeInGithubFeed}
 * via a single GitHub GraphQL request, cached and refreshed in the
 * background by {@link CachedFeed}.
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

	/**
	 * Empty payload returned before the first successful refresh and on
	 * total fetch failure with no prior data to fall back to.
	 */
	private static final GithubData EMPTY = new GithubData( List.of(), List.of(), List.of(), Map.of(), Map.of(), Map.of() );

	private final CachedFeed<GithubData> _feed;

	public GithubFeed( final Duration cacheDuration ) {
		_feed = new CachedFeed<>( cacheDuration, GithubFeed::fetch, EMPTY );
	}

	public List<OpenIssue> issues() {
		return _feed.value().issues();
	}

	public List<Release> releases() {
		return _feed.value().releases();
	}

	public List<Commit> commits() {
		return _feed.value().commits();
	}

	/**
	 * @return The repo's README.md rendered from HEAD, or null if it has none
	 */
	public String readmeFor( final Repo repo ) {
		return _feed.value().readmes().get( repo );
	}

	/**
	 * @return The repo's one-line description on GitHub, or null
	 */
	public String descriptionFor( final Repo repo ) {
		return _feed.value().descriptions().get( repo );
	}

	/**
	 * @return The repo's total number of open issues (not just the ones we list)
	 */
	public int openIssueCountFor( final Repo repo ) {
		return _feed.value().openIssueCounts().getOrDefault( repo, 0 );
	}

	/**
	 * @return Open issues across all tracked repos, from GitHub's totals
	 */
	public int openIssueCountTotal() {
		return _feed.value().openIssueCounts().values().stream().mapToInt( Integer::intValue ).sum();
	}

	public List<Commit> commitsFor( final Repo repo ) {
		return commits().stream().filter( c -> c.repo() == repo ).toList();
	}

	public List<Release> releasesFor( final Repo repo ) {
		return releases().stream().filter( r -> r.repo() == repo ).toList();
	}

	public List<OpenIssue> issuesFor( final Repo repo ) {
		return issues().stream().filter( i -> i.repo() == repo ).toList();
	}

	/**
	 * @return The most recent release for the repo, or null
	 */
	public Release latestReleaseFor( final Repo repo ) {
		final List<Release> list = releasesFor( repo );
		return list.isEmpty() ? null : list.get( 0 );
	}

	/**
	 * The combined payload from a single GraphQL fetch — kept together so
	 * one refresh repopulates everything atomically.
	 */
	public record GithubData(
			List<OpenIssue> issues,
			List<Release> releases,
			List<Commit> commits,
			Map<Repo, String> readmes,
			Map<Repo, String> descriptions,
			Map<Repo, Integer> openIssueCounts ) {}

	private static GithubData fetch() {
		final String token = WCCore.githubToken();

		if( token == null || token.isBlank() ) {
			System.err.println( "GithubFeed: wc.githubToken not set, skipping refresh" );
			return EMPTY;
		}

		final List<Repo> tracked = Repos.repos().stream()
				.filter( Repo::includeInGithubFeed )
				.toList();

		if( tracked.isEmpty() ) {
			return EMPTY;
		}

		try {
			final GithubGraphQLClient client = new GithubGraphQLClient( token );
			final String query = buildQuery( tracked );
			final JsonObject data = client.query( query );

			final List<RepoNode> repoNodes = deserializeRepoNodes( data, tracked.size() );

			return new GithubData(
					collectIssues( tracked, repoNodes ),
					collectReleases( tracked, repoNodes ),
					collectCommits( tracked, repoNodes ),
					collectReadmes( tracked, repoNodes ),
					collectDescriptions( tracked, repoNodes ),
					collectOpenIssueCounts( tracked, repoNodes ) );
		}
		catch( Exception e ) {
			// Re-throw so CachedFeed logs it and keeps the previous value
			throw new RuntimeException( e );
		}
	}

	/**
	 * Build a single GraphQL query that aliases each repo by its index, so we
	 * can map response fields back to the tracked repo list.
	 */
	private static String buildQuery( final List<Repo> repos ) {
		final String repoFragmentTemplate = """
				r%d: repository(owner: "%s", name: "%s") {
					description
					issues(states: OPEN, first: 6, orderBy: {field: UPDATED_AT, direction: DESC}) {
						totalCount
						nodes { number title url createdAt updatedAt author { login } }
					}
					releases(first: 20, orderBy: {field: CREATED_AT, direction: DESC}) {
						nodes { name tagName url createdAt isPrerelease isDraft description tagCommit { committedDate } }
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
					readme: object(expression: "HEAD:README.md") {
						... on Blob { text }
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
						n.createdAt(),
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
				// A release's createdAt is when the release OBJECT was made on GitHub, which for a
				// release published after the fact (a backfilled history, a tag released later) says
				// nothing about when the code shipped. The tagged commit's date does; prefer it.
				final Instant releasedAt = n.tagCommit() != null && n.tagCommit().committedDate() != null
						? n.tagCommit().committedDate()
						: n.createdAt();
				out.add( new Release(
						repos.get( i ),
						n.name(),
						n.tagName(),
						n.url(),
						releasedAt,
						n.description() ) );
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

	private static Map<Repo, String> collectReadmes( final List<Repo> repos, final List<RepoNode> repoNodes ) {
		final Map<Repo, String> out = new HashMap<>();
		for( int i = 0; i < repos.size(); i++ ) {
			final RepoNode rn = repoNodes.get( i );
			if( rn == null || rn.readme() == null || rn.readme().text() == null ) continue;
			out.put( repos.get( i ), rn.readme().text() );
		}
		return Map.copyOf( out );
	}

	private static Map<Repo, String> collectDescriptions( final List<Repo> repos, final List<RepoNode> repoNodes ) {
		final Map<Repo, String> out = new HashMap<>();
		for( int i = 0; i < repos.size(); i++ ) {
			final RepoNode rn = repoNodes.get( i );
			if( rn == null || rn.description() == null || rn.description().isBlank() ) continue;
			out.put( repos.get( i ), rn.description() );
		}
		return Map.copyOf( out );
	}

	private static Map<Repo, Integer> collectOpenIssueCounts( final List<Repo> repos, final List<RepoNode> repoNodes ) {
		final Map<Repo, Integer> out = new HashMap<>();
		for( int i = 0; i < repos.size(); i++ ) {
			final RepoNode rn = repoNodes.get( i );
			if( rn == null || rn.issues() == null ) continue;
			out.put( repos.get( i ), rn.issues().totalCount() );
		}
		return Map.copyOf( out );
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
