package whoacommunity.github;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import whoacommunity.util.Repos.Repo;

/**
 * A published GitHub release across our tracked repos.
 *
 * Note on the date field: we use GitHub's {@code createdAt} rather than
 * {@code publishedAt}. GitHub's API silently ignores attempts to backdate
 * {@code publishedAt}, but when a release is created via API against an
 * existing tag, {@code createdAt} is set to the tagged commit's date —
 * which is what we want to show as "the release date".
 */
public record Release(
		Repo repo,
		String name,
		String tagName,
		String url,
		Instant createdAt ) {

	/**
	 * @return The display name — falls back to the tag if release name is empty
	 */
	public String displayName() {
		return ( name == null || name.isBlank() ) ? tagName : name;
	}

	public String shortDateFormatted() {
		return DateTimeFormatter.ofPattern( "MMM d" ).withZone( ZoneId.systemDefault() ).format( createdAt );
	}
}
