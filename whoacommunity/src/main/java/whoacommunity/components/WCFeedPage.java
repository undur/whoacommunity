package whoacommunity.components;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Org;
import whoacommunity.util.Repos.Repo;

public class WCFeedPage extends WCComponent {

	public Org currentOrg;
	public Repo currentRepo;

	/**
	 * The repo the user has filtered the commit list down to, or null when
	 * showing all commits.
	 */
	public Repo selectedRepo;

	/**
	 * Old RSS-backed feed kept around as fallback infrastructure; the live
	 * commit list is now served from {@link GithubFeed}.
	 */
	public static OurFeed feed = new OurFeed( Duration.ofMinutes( 1 ) );

	public WCFeedPage( NGContext context ) {
		super( context );
	}

	public Org[] orgs() {
		return Org.values();
	}

	/**
	 * @return All commits across the tracked repos, optionally filtered by
	 *         the selected repo. The dev-feed page shows the full list, the
	 *         sidebar (via WCComponent.items()) trims to 10.
	 */
	public List<Commit> allCommits() {
		final List<Commit> all = GithubFeed.shared.commits();

		if( selectedRepo == null ) {
			return all;
		}

		return all.stream().filter( c -> c.repo() == selectedRepo ).toList();
	}

	public NGActionResults selectRepo() {
		// Clicking the funnel of the already-filtered repo clears the filter
		selectedRepo = ( currentRepo == selectedRepo ) ? null : currentRepo;
		return null;
	}

	public NGActionResults clearFilter() {
		selectedRepo = null;
		return null;
	}

	public boolean isCurrentRepoSelected() {
		return currentRepo == selectedRepo;
	}

	public String currentRepoClass() {
		return isCurrentRepoSelected() ? "is-active" : null;
	}

	public static class OurFeed {

		/**
		 * The amount of time to cache the contents of the feed before refetching
		 */
		private final Duration _cacheDuration;

		/**
		 * The time at which this feed was last refreshed
		 */
		private Instant _lastRefreshed;

		/**
		 * Cached items
		 */
		private List<OurItem> _items;

		public OurFeed( final Duration cacheDuration ) {
			_cacheDuration = cacheDuration;
		}

		boolean shouldRefresh() {

			// Force refresh if it's never been refreshed, or cache duration is set to null (no caching)
			if( _lastRefreshed == null || _cacheDuration == null ) {
				return true;
			}

			final boolean cacheExpired = Duration.between( _lastRefreshed, Instant.now() ).compareTo( _cacheDuration ) > 0;

			return cacheExpired;
		}

		public List<OurItem> items() {
			if( shouldRefresh() ) {
				_items = fetchItems();
				_lastRefreshed = Instant.now();
				System.out.println( "Refreshing cache!" );
			}

			return _items;
		}

		private List<OurItem> fetchItems() {
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
			 * @return Date for display
			 */
			public String shortDateFormatted() {
				return item().getPubDateZonedDateTime().get().format( DateTimeFormatter.ofPattern( "MMM d" ) );
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
}