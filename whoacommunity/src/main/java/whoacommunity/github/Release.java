package whoacommunity.github;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import whoacommunity.util.Repos.Repo;

/**
 * A published GitHub release across our tracked repos.
 */
public record Release(
		Repo repo,
		String name,
		String tagName,
		String url,
		Instant publishedAt ) {

	/**
	 * @return The display name — falls back to the tag if release name is empty
	 */
	public String displayName() {
		return ( name == null || name.isBlank() ) ? tagName : name;
	}

	public String shortDateFormatted() {
		return DateTimeFormatter.ofPattern( "MMM d" ).withZone( ZoneId.systemDefault() ).format( publishedAt );
	}
}
