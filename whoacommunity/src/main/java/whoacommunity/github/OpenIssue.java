package whoacommunity.github;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import whoacommunity.util.Repos.Repo;

/**
 * A single open issue across our tracked repos.
 */
public record OpenIssue(
		Repo repo,
		int number,
		String title,
		String url,
		Instant updatedAt,
		String authorLogin ) {

	public String shortDateFormatted() {
		return DateTimeFormatter.ofPattern( "MMM d" ).withZone( ZoneId.systemDefault() ).format( updatedAt );
	}
}
