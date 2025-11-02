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

	public WCSidebar( NGContext context ) {
		super( context );
	}

	public List<JavaItem> javaItems() {
		try {
			return new RssReader()
					.read( "https://inside.java/feed.xml" )
					.sorted()
					.map( JavaItem::new )
					.toList();
		}
		catch( IOException e ) {
			return List.of();
		}
	}

	public record JavaItem( Item item ) {

		public String title() {
			return item.getTitle().get();
		}

		public String link() {
			return item.getLink().get();
		}

		/**
		 * @return Date for display
		 */
		public String shortDateFormatted() {
			return item().getPubDateZonedDateTime().get().format( DateTimeFormatter.ofPattern( "MMM d" ) );
		}
	}
}