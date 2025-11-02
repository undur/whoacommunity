package whoacommunity.components;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

public class WCSidebar extends WCComponent {

	public JavaItem javaItem;
	private static List<JavaItem> _javaItems;

	public WCSidebar( NGContext context ) {
		super( context );
	}

	/**
	 * FIXME: We're temporarily caching this forever for performance, add a more intelligent cache // Hugi 2025-11-02
	 */
	public List<JavaItem> javaItems() {
		if( _javaItems == null ) {
			try {
				_javaItems = new RssReader()
						.read( "https://inside.java/feed.xml" )
						.sorted()
						.map( JavaItem::new )
						.toList();
			}
			catch( IOException e ) {
				_javaItems = List.of();
			}
		}

		return _javaItems;
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