package whoacommunity.components;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.CachedFeed;

public class WCSidebar extends WCComponent {

	public JavaItem javaItem;

	private static final CachedFeed<List<JavaItem>> _javaFeed = new CachedFeed<>(
			Duration.ofMinutes( 15 ),
			WCSidebar::fetchJavaItems,
			List.of() );

	public WCSidebar( NGContext context ) {
		super( context );
	}

	public List<JavaItem> javaItems() {
		return _javaFeed.value();
	}

	private static List<JavaItem> fetchJavaItems() {
		try {
			return new RssReader()
					.read( "https://inside.java/feed.xml" )
					.sorted()
					.map( JavaItem::new )
					.toList();
		}
		catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * FIXME: use a single wrapper class (or at least a common interface) for our RSS items // Hugi 2025-11-02
	 */
	public record JavaItem( Item item ) {

		public String title() {
			return item.getTitle().get();
		}

		public String link() {
			return item.getLink().get();
		}

		public String shortDateFormatted() {
			return item().getPubDateZonedDateTime().get().format( DateTimeFormatter.ofPattern( "MMM d" ) );
		}
	}
}