package whoacommunity.github;

import java.time.Instant;

import whoacommunity.util.Dates;
import whoacommunity.util.Markdown;
import whoacommunity.util.Repos.Repo;

/**
 * A published GitHub release across our tracked repos.
 *
 * Note on the date field: {@code createdAt} here is the date of the commit the
 * release's tag points at, not the release object's own creation date. A release
 * published after the fact (a backfilled history, a tag released later) is created
 * on GitHub long after the code shipped; the tagged commit says when it really did.
 */
public record Release(
		Repo repo,
		String name,
		String tagName,
		String url,
		Instant createdAt,
		String body ) {

	/**
	 * @return true when the release has release notes worth showing
	 */
	public boolean hasBody() {
		return body != null && !body.isBlank();
	}

	/**
	 * @return The release notes rendered from GitHub's Markdown, or null when there are none
	 */
	public String bodyAsHTML() {
		return hasBody() ? Markdown.render( body ) : null;
	}

	/**
	 * @return An id safe to use as an in-page anchor for this release, derived from the tag
	 */
	public String anchorId() {
		return "release-" + tagName.replaceAll( "[^A-Za-z0-9._-]", "-" );
	}

	/**
	 * @return The display name — falls back to the tag if release name is empty
	 */
	public String displayName() {
		return ( name == null || name.isBlank() ) ? tagName : name;
	}

	public String shortDateFormatted() {
		return Dates.shortDate( createdAt );
	}
}
