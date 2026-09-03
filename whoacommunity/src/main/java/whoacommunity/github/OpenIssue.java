package whoacommunity.github;

import java.time.Instant;

import whoacommunity.util.Dates;
import whoacommunity.util.Repos.Repo;

/**
 * A single open issue across our tracked repos.
 */
public record OpenIssue(
		Repo repo,
		int number,
		String title,
		String url,
		Instant createdAt,
		Instant updatedAt,
		String authorLogin ) {

	public String shortDateFormatted() {
		return Dates.shortDate( updatedAt );
	}
}
