package whoacommunity.github;

import java.time.Instant;
import java.util.List;

/**
 * Typed view of the per-repo node returned by our GraphQL query. Used only
 * to deserialize from JSON via Gson; field names match the GraphQL schema
 * (or our aliases) one-for-one.
 *
 * Anything missing in the response is left null/0/false by Gson, which is
 * the behavior we want.
 */
final class GithubResponse {

	private GithubResponse() {}

	record RepoNode(
			String description,
			IssueConnection issues,
			ReleaseConnection releases,
			DefaultBranchRef defaultBranchRef,
			Readme readme,
			RootTree rootTree ) {}

	/** The "rootTree" alias: the tree at HEAD, so the project page can list the repository's top level */
	record RootTree( List<TreeEntryNode> entries ) {}

	/** One entry of a tree: type is "blob" for a file, "tree" for a directory */
	record TreeEntryNode( String name, String type ) {}

	/** The "readme" alias: the README.md blob at HEAD, or null if the repo has none */
	record Readme( String text ) {}

	record IssueConnection( int totalCount, List<IssueNode> nodes ) {}

	record IssueNode(
			int number,
			String title,
			String url,
			Instant createdAt,
			Instant updatedAt,
			Author author ) {}

	record Author( String login ) {}

	record ReleaseConnection( List<ReleaseNode> nodes ) {}

	/** The commit a release's tag points at — its date is when the released code was made. */
	record TagCommit( Instant committedDate ) {}

	record ReleaseNode(
			String name,
			String tagName,
			String url,
			Instant createdAt,
			boolean isPrerelease,
			boolean isDraft,
			String description,
			TagCommit tagCommit ) {}

	record DefaultBranchRef( CommitTarget target ) {}

	record CommitTarget( CommitConnection history ) {}

	record CommitConnection( List<CommitNode> nodes ) {}

	record CommitNode(
			String messageHeadline,
			String url,
			Instant committedDate,
			CommitAuthor author ) {}

	record CommitAuthor( String name, GithubUser user ) {}

	record GithubUser( String login ) {}
}
