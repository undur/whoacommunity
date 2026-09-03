package whoacommunity.util;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Date formatting shared by the feed records
 */
public class Dates {

	private static final DateTimeFormatter THIS_YEAR = DateTimeFormatter.ofPattern( "MMM d" ).withZone( ZoneId.systemDefault() );
	private static final DateTimeFormatter OTHER_YEAR = DateTimeFormatter.ofPattern( "MMM d, yyyy" ).withZone( ZoneId.systemDefault() );

	/**
	 * @return "Sep 3" for a date in the current year, "Sep 3, 2019" otherwise — some repos haven't moved in a while
	 */
	public static String shortDate( final Instant instant ) {
		final int year = instant.atZone( ZoneId.systemDefault() ).getYear();
		final DateTimeFormatter formatter = year == Year.now( ZoneId.systemDefault() ).getValue() ? THIS_YEAR : OTHER_YEAR;
		return formatter.format( instant );
	}
}
