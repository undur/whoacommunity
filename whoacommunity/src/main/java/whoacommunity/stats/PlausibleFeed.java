package whoacommunity.stats;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ng.appserver.NGApplication;
import ng.plausible.NGPlausible;
import whoacommunity.app.WCCore;
import whoacommunity.util.CachedFeed;

/**
 * The site's traffic as Plausible sees it, for the card on the front page: visits (sessions), visitors and page
 * views per day for the last 30 days, their totals, and the number of people on the site right now. Visits is the
 * headline figure: Plausible's visitor identity resets daily, so visitors summed over days counts the same person
 * again each day, while a visit is a session (ended by 30 minutes of inactivity) and adds up across days. Fetched from the Stats API of
 * the Plausible server ng-plausible is configured for, with the key in wc.plausibleApiKey. Without a key the feed
 * stays empty and the card isn't shown.
 */
public class PlausibleFeed {

	public static final int DAYS = 30;

	public static final PlausibleFeed shared = new PlausibleFeed( Duration.ofMinutes( 15 ) );

	private static final ZoneId SITE_ZONE = ZoneId.of( "Atlantic/Reykjavik" );
	private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout( Duration.ofSeconds( 5 ) ).build();
	private static boolean _warnedAboutKey;

	private final CachedFeed<Stats> _feed;

	public PlausibleFeed( final Duration cacheDuration ) {
		_feed = new CachedFeed<>( cacheDuration, PlausibleFeed::fetch, null );
	}

	public record Day( LocalDate date, int visitors, int visits, int pageviews ) {}

	/**
	 * @param days One entry per day, oldest first, every day present (zero for days without visits)
	 * @param now People on the site right now, or null if that call failed
	 */
	public record Stats( List<Day> days, int visitors, int visits, int pageviews, Integer now ) {

		/**
		 * @return Visitors per day, averaged from the first day that saw any traffic (the chart's earliest days are
		 *         empty until tracking has been running for 30 days, and those shouldn't drag the average down).
		 *         Summed visitors is misleading since Plausible counts the same person again each day; the daily
		 *         figure is the one that means something.
		 */
		public int visitorsPerDay() {
			int counted = 0;
			int sum = 0;

			for( final Day day : days ) {
				if( counted == 0 && day.visits() == 0 ) {
					continue;
				}

				counted++;
				sum += day.visitors();
			}

			return counted == 0 ? 0 : Math.round( sum / (float)counted );
		}
	}

	public Stats stats() {
		return _feed.value();
	}

	public boolean isLoaded() {
		return stats() != null;
	}

	/**
	 * @return The dashboard for the site on the Plausible server
	 */
	public static String dashboardURL() {
		return NGPlausible.serverURL() + "/" + NGPlausible.domain();
	}

	private static Stats fetch() {
		// The sweep starts in Application.load(), once ng has its application reference and every plugin has read its
		// properties; this guard is a safety net in case a fetch ever runs earlier than that
		if( NGApplication.application() == null ) {
			throw new CachedFeed.NotReadyException();
		}

		final String key = WCCore.plausibleApiKey();
		final String problem = key == null || key.isBlank() ? "wc.plausibleApiKey not set" : !NGPlausible.isConfigured() ? "ng-plausible not configured (ng.plausible.url and ng.plausible.domain)" : null;

		if( problem != null ) {
			if( !_warnedAboutKey ) {
				System.err.println( "PlausibleFeed: " + problem + ", the traffic card stays hidden" );
				_warnedAboutKey = true;
			}

			return null;
		}

		try {
			final LocalDate today = LocalDate.now( SITE_ZONE );
			final LocalDate first = today.minusDays( DAYS - 1 );

			final Map<LocalDate, Day> byDate = new HashMap<>();

			for( final JsonElement e : queryByDay( key, first, today ) ) {
				final JsonObject row = e.getAsJsonObject();
				final LocalDate date = LocalDate.parse( row.getAsJsonArray( "dimensions" ).get( 0 ).getAsString().substring( 0, 10 ) );
				final JsonArray metrics = row.getAsJsonArray( "metrics" );
				byDate.put( date, new Day( date, metrics.get( 0 ).getAsInt(), metrics.get( 1 ).getAsInt(), metrics.get( 2 ).getAsInt() ) );
			}

			// Every day gets an entry, so the chart never skips a quiet day
			final List<Day> days = new ArrayList<>( DAYS );
			int visitors = 0, visits = 0, pageviews = 0;

			for( LocalDate d = first; !d.isAfter( today ); d = d.plusDays( 1 ) ) {
				final Day day = byDate.getOrDefault( d, new Day( d, 0, 0, 0 ) );
				days.add( day );
				visitors += day.visitors();
				visits += day.visits();
				pageviews += day.pageviews();
			}

			return new Stats( List.copyOf( days ), visitors, visits, pageviews, realtimeVisitors( key ) );
		}
		catch( final Exception e ) {
			System.err.println( "PlausibleFeed: refresh failed, keeping the previous value: " + e );
			throw new RuntimeException( e );
		}
	}

	private static JsonArray queryByDay( final String key, final LocalDate first, final LocalDate last ) throws Exception {
		final JsonObject body = new JsonObject();
		body.addProperty( "site_id", NGPlausible.domain() );
		final JsonArray metrics = new JsonArray();
		metrics.add( "visitors" );
		metrics.add( "visits" );
		metrics.add( "pageviews" );
		body.add( "metrics", metrics );
		final JsonArray range = new JsonArray();
		range.add( first.toString() );
		range.add( last.toString() );
		body.add( "date_range", range );
		final JsonArray dimensions = new JsonArray();
		dimensions.add( "time:day" );
		body.add( "dimensions", dimensions );

		final HttpRequest request = HttpRequest.newBuilder( URI.create( NGPlausible.serverURL() + "/api/v2/query" ) )
				.timeout( Duration.ofSeconds( 10 ) )
				.header( "Authorization", "Bearer " + key )
				.header( "Content-Type", "application/json" )
				.POST( HttpRequest.BodyPublishers.ofString( new Gson().toJson( body ) ) )
				.build();

		final HttpResponse<String> response = CLIENT.send( request, HttpResponse.BodyHandlers.ofString() );

		if( response.statusCode() != 200 ) {
			throw new IllegalStateException( "Plausible answered " + response.statusCode() + ": " + response.body() );
		}

		return new Gson().fromJson( response.body(), JsonObject.class ).getAsJsonArray( "results" );
	}

	/**
	 * @return Visitors in the last few minutes, or null if the call fails; this one is allowed to fail quietly
	 */
	private static Integer realtimeVisitors( final String key ) {
		try {
			final HttpRequest request = HttpRequest.newBuilder( URI.create( NGPlausible.serverURL() + "/api/v1/stats/realtime/visitors?site_id=" + NGPlausible.domain() ) )
					.timeout( Duration.ofSeconds( 5 ) )
					.header( "Authorization", "Bearer " + key )
					.GET()
					.build();

			final HttpResponse<String> response = CLIENT.send( request, HttpResponse.BodyHandlers.ofString() );
			return response.statusCode() == 200 ? Integer.valueOf( response.body().trim() ) : null;
		}
		catch( final Exception e ) {
			return null;
		}
	}

	/**
	 * The chart: one bar per day, scaled to the busiest day, stretched to the card's width by the stylesheet. Each
	 * bar is the day's visits, with its visitors drawn solid at the bottom (a visitor has at least one visit, so
	 * that part never exceeds the bar). Built here rather than in a template since bar heights are per-row
	 * attributes. Quiet days keep a one-unit tick so the baseline reads as 30 days, not as a gap.
	 */
	public static String svg( final List<Day> days ) {
		final int height = 40;
		final int max = Math.max( 1, days.stream().mapToInt( Day::visits ).max().orElse( 0 ) );
		final StringBuilder sb = new StringBuilder();
		sb.append( "<svg class=\"rail-chart-svg\" viewBox=\"0 0 " ).append( days.size() * 10 ).append( " " ).append( height ).append( "\" preserveAspectRatio=\"none\" aria-hidden=\"true\">" );

		int x = 0;

		for( final Day day : days ) {
			final int visitsHeight = Math.max( 1, Math.round( ( height - 2 ) * day.visits() / (float)max ) );
			final int visitorsHeight = Math.min( visitsHeight, Math.round( ( height - 2 ) * day.visitors() / (float)max ) );
			sb.append( "<g><title>" ).append( day.date() ).append( ": " ).append( day.visits() ).append( day.visits() == 1 ? " visit, " : " visits, " ).append( day.visitors() ).append( day.visitors() == 1 ? " visitor" : " visitors" ).append( "</title>" );
			sb.append( "<rect class=\"visits\" x=\"" ).append( x + 1 ).append( "\" y=\"" ).append( height - visitsHeight ).append( "\" width=\"8\" height=\"" ).append( visitsHeight ).append( "\" rx=\"1\" />" );

			if( visitorsHeight > 0 ) {
				sb.append( "<rect class=\"visitors\" x=\"" ).append( x + 1 ).append( "\" y=\"" ).append( height - visitorsHeight ).append( "\" width=\"8\" height=\"" ).append( visitorsHeight ).append( "\" rx=\"1\" />" );
			}

			sb.append( "</g>" );
			x += 10;
		}

		return sb.append( "</svg>" ).toString();
	}
}
