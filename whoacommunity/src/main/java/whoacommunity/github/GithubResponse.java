package whoacommunity.github;

import java.time.Instant;
import java.util.List;

/**
 * Typed view of the per-repo node returned by our GraphQL query. Used only
 * to deserialize from JSON via Gson; field names match the GraphQL schema
 * one-for-one.
 *
 * Anything missing in the response is left null/0/false by Gson, which is
 * the behavior we want.
 */
final class GithubResponse {

	private GithubResponse() {}

	record RepoNode(
			IssueConnection issues,
			ReleaseConnection releases,
			DefaultBranchRef defaultBranchRef ) {}

	record IssueConnection( List<IssueNode> nodes ) {}

	record IssueNode(
			int number,
			String title,
			String url,
			Instant updatedAt,
			Author author ) {}

	record Author( String login ) {}

	record ReleaseConnection( List<ReleaseNode> nodes ) {}

	record ReleaseNode(
			String name,
			String tagName,
			String url,
			Instant createdAt,
			boolean isPrerelease,
			boolean isDraft ) {}

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
