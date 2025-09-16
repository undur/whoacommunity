package whoacommunity.components;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Org;
import whoacommunity.util.Repos.Repo;

public class WCFeedPage extends WCComponent {

	public OurItem current;
	public Org currentOrg;
	public Repo currentRepo;

	public WCFeedPage( NGContext context ) {
		super( context );
	}

	public Org[] orgs() {
		return Org.values();
	}

	public List<OurItem> items() {
		final List<String> urls = Repos.repos()
				.stream()
				.map( Repo::commitsAtomURL )
				.toList();

		return new RssReader()
				.read( urls )
				.sorted()
				.map( OurItem::new )
				.toList();
	}

	/**
	 * Wrapper for the atom feed's item so we can enrich it with a bit of our own data
	 */
	public record OurItem( Item item ) {

		/**
		 * @return The repo from which this message originated
		 */
		public Repo repo() {
			for( Repo repo : Repos.repos() ) {
				if( item.getLink().get().startsWith( repo.url() + "/" ) ) {
					return repo;
				}
			}

			return null;
		}

		/**
		 * @return Date for display
		 */
		public String dateFormatted() {
			return item().getPubDateZonedDateTime().get().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
		}

		/**
		 * Hide away the Optional
		 */
		public String link() {
			return item().getLink().orElse( null );
		}

		/**
		 * Hide away the Optional
		 */
		public String title() {
			return item().getTitle().orElse( null );
		}

		/**
		 * Hide away the Optional
		 */
		public String author() {
			final String author = item().getAuthor().orElse( null );
			return "hugithordarson".equals( author ) ? "hugi" : author;
		}
	}
}