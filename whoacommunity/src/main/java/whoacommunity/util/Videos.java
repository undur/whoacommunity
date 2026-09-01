package whoacommunity.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Our YouTube videos, grouped by playlist. Hardcoded for now — the source of
 * truth is the channel at https://www.youtube.com/@hugithordarson; refresh
 * this list from the playlists' RSS feeds
 * (https://www.youtube.com/feeds/videos.xml?playlist_id=…) when videos are added.
 */
public class Videos {

	private static final List<Playlist> _playlists = List.of(
			new Playlist( "PLoqbqfn2kulb6pQ6KSUU_9tLiW1xb-cJy", "WebObjects related stuff", List.of(
					new Video( "hoW1WusKSRo", LocalDate.of( 2026, 7, 6 ), "Inline SQL logging/profiling in Parsley templates, enabled by Cayenne's awesome query logging" ),
					new Video( "B5rqoiCsLac", LocalDate.of( 2026, 6, 28 ), "Declaration of dynamic tags and element patches in Parsley" ),
					new Video( "Sni0I8is1ZU", LocalDate.of( 2026, 6, 24 ), "Element Reference View in Parsley Template Editor" ),
					new Video( "CynrrftqHSg", LocalDate.of( 2026, 6, 23 ), "Extended Element API view in Eclipse/Parsley Template Editor" ),
					new Video( "fLmE_vGm2-w", LocalDate.of( 2026, 6, 2 ), "Parsley Render Tree" ),
					new Video( "eYlTcdyMtOY", LocalDate.of( 2026, 5, 30 ), "More context for WO exceptions using Parsley" ),
					new Video( "QGxO0EsQukM", LocalDate.of( 2026, 3, 2 ), "Parsley: Refactoring in templates when renaming bindings" ),
					new Video( "iDQcMVRUhyk", LocalDate.of( 2025, 11, 9 ), "Running a plain maven app with Wonder 7.5-SNAPSHOT without \"Generate Bundles\"" ),
					new Video( "OwL2PRel0mU", LocalDate.of( 2025, 3, 25 ), "Parsley inline render error display" ),
					new Video( "Ahu3Qnki1-w", LocalDate.of( 2025, 2, 1 ), "Getting started with Cayenne in WebObjects" ),
					new Video( "4-uUxO1Ev74", LocalDate.of( 2024, 11, 8 ), "Monitor 2024-11-08" ),
					new Video( "x6AM3HSms9U", LocalDate.of( 2024, 5, 10 ), "Concurrent rendering in WO" ) ) ),

			new Playlist( "PLoqbqfn2kulbYh6w6YUKAFDGgRv7PZLAR", "ng-objects related stuff", List.of(
					new Video( "zg3e7S57XFM", LocalDate.of( 2026, 3, 7 ), "First test using ng-objects from Maven Central" ),
					new Video( "3vypLdi_nfE", LocalDate.of( 2025, 9, 23 ), "Importing and running ng-testapp in Eclipse" ),
					new Video( "7GZuJ6lnQp8", LocalDate.of( 2024, 11, 17 ), "Rendering non-existent elements/components" ),
					new Video( "-obvt93wSFc", LocalDate.of( 2024, 4, 27 ), "ng preview" ),
					new Video( "CCM6E4UbN44", LocalDate.of( 2022, 4, 21 ), "Templating" ) ) ) );

	public static List<Playlist> playlists() {
		return _playlists;
	}

	public static Optional<Video> videoWithID( final String youtubeID ) {
		return _playlists
				.stream()
				.flatMap( p -> p.videos().stream() )
				.filter( v -> v.youtubeID().equals( youtubeID ) )
				.findFirst();
	}

	public static Optional<Playlist> playlistContaining( final Video video ) {
		return _playlists
				.stream()
				.filter( p -> p.videos().contains( video ) )
				.findFirst();
	}

	public record Playlist( String youtubeID, String title, List<Video> videos ) {

		public String url() {
			return "https://www.youtube.com/playlist?list=" + youtubeID();
		}
	}

	public record Video( String youtubeID, LocalDate published, String title ) {

		/**
		 * @return Our own page for the video
		 */
		public String pageURL() {
			return "/video/" + youtubeID();
		}

		public String youtubeURL() {
			return "https://www.youtube.com/watch?v=" + youtubeID();
		}

		/**
		 * The privacy-enhanced embed host: no tracking cookies until playback starts
		 */
		public String embedURL() {
			return "https://www.youtube-nocookie.com/embed/" + youtubeID();
		}

		public String formattedDate() {
			return published().format( DateTimeFormatter.ofPattern( "MMMM d, yyyy" ) );
		}
	}
}
