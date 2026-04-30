package whoacommunity.github;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import whoacommunity.util.Repos.Repo;

/**
 * A single commit on a tracked repo's default branch.
 *
 * Surface mirrors the older RSS-based OurItem so existing template
 * bindings (repo / title / link / author / dateFormatted / shortDateFormatted)
 * keep working without changes.
 */
public record Commit(
		Repo repo,
		String title,
		String link,
		String author,
		Instant committedAt ) {

	public String dateFormatted() {
		return DateTimeFormatter.ofPattern( "yyyy-MM-dd" ).withZone( ZoneId.systemDefault() ).format( committedAt );
	}

	public String shortDateFormatted() {
		return DateTimeFormatter.ofPattern( "MMM d" ).withZone( ZoneId.systemDefault() ).format( committedAt );
	}
}
