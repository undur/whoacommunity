package whoacommunity.components;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The WOWODC 2026 banner at the top of the front page, in the conference site's own look: dates, place and how
 * far off it is. Shown until the conference ends, then it renders nothing.
 */
public class WCConferenceBanner extends WCComponent {

	public static final String URL = "https://wowodc.com/";
	public static final String TICKETS_URL = "https://wowodc.com/#tickets";

	/**
	 * The conference runs Friday to Sunday, 6–8 November 2026, in Munich
	 */
	static final LocalDate FIRST_DAY = LocalDate.of( 2026, 11, 6 );
	static final LocalDate LAST_DAY = LocalDate.of( 2026, 11, 8 );

	private static final ZoneId MUNICH = ZoneId.of( "Europe/Berlin" );

	public WCConferenceBanner( NGContext context ) {
		super( context );
	}

	static LocalDate today() {
		return LocalDate.now( MUNICH );
	}

	/**
	 * @return true until the last day of the conference has passed
	 */
	public boolean isShown() {
		return isShown( today() );
	}

	public static boolean isShown( final LocalDate day ) {
		return !day.isAfter( LAST_DAY );
	}

	/**
	 * @return "in 21 days", "tomorrow", "happening now"
	 */
	public String countdown() {
		return countdown( today() );
	}

	public static String countdown( final LocalDate day ) {
		final long days = ChronoUnit.DAYS.between( day, FIRST_DAY );

		if( days > 1 ) {
			return "in " + days + " days";
		}

		if( days == 1 ) {
			return "tomorrow";
		}

		return "happening now";
	}

	public boolean isHappeningNow() {
		return !today().isBefore( FIRST_DAY );
	}

	public String url() {
		return URL;
	}

	public String ticketsURL() {
		return TICKETS_URL;
	}
}
