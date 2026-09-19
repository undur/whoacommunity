package whoacommunity.components;

import java.time.Instant;
import java.util.Comparator;
import java.util.ArrayList;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.OpenIssue;
import whoacommunity.github.Release;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * Development activity: commits, releases and open issues across the tracked
 * repos, one tab each, filterable by repository from the rail.
 */
public class WCFeedPage extends WCComponent {

	/**
	 * The filter rail groups repos as the stack, others we follow, and the repos around the stack (this site, tooling) —
	 * the last group is left out of the unfiltered view so its commits don't drown the stack's
	 */
	public record RepoGroup( String name, List<Repo> repos ) {}

	public enum Tab {
		all( "All", "commits, releases and issues from the last 60 days", "One stream: commits as they happen, with the releases they became and the issues that prompted them. Issues are placed by the day they were opened. The counts in the filter are commits in the last seven days." ),
		commits( "Commits", "50 most recent commits each", "Only the 50 most recent commits are fetched per repository, so this isn't the full history — it's what GitHub's feed gives us. The counts in the filter are commits in the last seven days." ),
		releases( "Releases", "20 most recent releases each", "Only the twenty most recent published releases are fetched per repository." ),
		issues( "Open issues", "6 most recently updated each", "Only the six most recently updated open issues are fetched per repository — the count in the rail is the true total." );

		public final String title;
		public final String scope;
		public final String note;

		Tab( String title, String scope, String note ) {
			this.title = title;
			this.scope = scope;
			this.note = note;
		}
	}

	public RepoGroup currentGroup;
	public Repo currentRepo;
	public Tab currentTab;

	/**
	 * Which of the three lists is showing
	 */
	public Tab tab = Tab.all;

	/**
	 * The repos whose activity is in the feed. Starts as the default set (everything but "around the stack");
	 * clicking a repo in the rail toggles it, "only" narrows to that one repo, "reset" restores the default.
	 */
	private final Set<Repo> _shown = new LinkedHashSet<>( defaultRepos() );

	private static List<Repo> defaultRepos() {
		return Repos.repos().stream().filter( Repos::inStreams ).toList();
	}

	public WCFeedPage( NGContext context ) {
		super( context );

		// /dev-feed?tab=releases opens on that tab, so the front page rail can deep-link
		final String requested = context.request().formValueForKey( "tab" );

		if( requested != null ) {
			for( Tab t : Tab.values() ) {
				if( t.name().equals( requested ) ) {
					tab = t;
				}
			}
		}
	}

	@Override
	public String pageIdentifier() {
		return "activity";
	}

	@Override
	public String breadcrumbLeaf() {
		return "activity";
	}

	public List<Tab> tabs() {
		return List.of( Tab.values() );
	}

	public NGActionResults selectTab() {
		tab = currentTab;
		return null;
	}

	public String tabClass() {
		return currentTab == tab ? "is-on" : "";
	}

	/**
	 * One row of the merged stream: a commit, a release or an issue, placed on one timeline
	 */
	public record FeedItem( String kind, Repo repo, Instant when, String title, String url, String who ) {

		public String shortDateFormatted() {
			return whoacommunity.util.Dates.shortDate( when );
		}

		public boolean isRelease() {
			return "release".equals( kind );
		}

		public boolean isIssue() {
			return "issue".equals( kind );
		}

		public String rowClass() {
			return "row-" + kind;
		}
	}

	public FeedItem currentItem;

	private static final Duration ALL_WINDOW = Duration.ofDays( 60 );

	/**
	 * @return Commits, releases and issues from the shown repos, newest first, limited to the last 60 days so no one kind's fetch limit shapes the tail
	 */
	public List<FeedItem> allItems() {
		final Instant since = Instant.now().minus( ALL_WINDOW );
		final List<FeedItem> out = new ArrayList<>();

		for( Commit c : GithubFeed.shared.commits() ) {
			if( _shown.contains( c.repo() ) && c.committedAt().isAfter( since ) ) {
				out.add( new FeedItem( "commit", c.repo(), c.committedAt(), c.title(), c.link(), c.author() ) );
			}
		}

		for( Release r : GithubFeed.shared.releases() ) {
			if( _shown.contains( r.repo() ) && r.createdAt() != null && r.createdAt().isAfter( since ) ) {
				out.add( new FeedItem( "release", r.repo(), r.createdAt(), r.displayName(), r.url(), r.tagName() ) );
			}
		}

		for( OpenIssue i : GithubFeed.shared.issues() ) {
			if( _shown.contains( i.repo() ) && i.createdAt() != null && i.createdAt().isAfter( since ) ) {
				out.add( new FeedItem( "issue", i.repo(), i.createdAt(), i.title(), i.url(), i.authorLogin() ) );
			}
		}

		out.sort( Comparator.comparing( FeedItem::when ).reversed() );
		return out;
	}

	public boolean isAll() {
		return tab == Tab.all;
	}

	public boolean isCommits() {
		return tab == Tab.commits;
	}

	public boolean isReleases() {
		return tab == Tab.releases;
	}

	public boolean isIssues() {
		return tab == Tab.issues;
	}

	public List<RepoGroup> groups() {
		return List.of(
				new RepoGroup( Repos.Group.stack.title, Repos.reposIn( Repos.Group.stack ) ),
				new RepoGroup( Repos.Group.around.title, Repos.reposIn( Repos.Group.around ) ),
				new RepoGroup( Repos.Group.others.title, Repos.reposIn( Repos.Group.others ) ) );
	}

	/**
	 * @return All commits across the tracked repos, optionally filtered by the selected repo
	 */
	public List<Commit> allCommits() {
		return GithubFeed.shared.commits().stream().filter( c -> _shown.contains( c.repo() ) ).toList();
	}

	public List<Release> allReleases() {
		return GithubFeed.shared.releases().stream().filter( r -> _shown.contains( r.repo() ) ).toList();
	}

	public List<OpenIssue> allIssues() {
		return GithubFeed.shared.issues().stream().filter( i -> _shown.contains( i.repo() ) ).toList();
	}

	/**
	 * @return true when the shown set differs from the default, so the "reset" link appears
	 */
	public boolean hasFilter() {
		return !_shown.equals( new LinkedHashSet<>( defaultRepos() ) );
	}

	public String filterDescription() {
		if( !hasFilter() ) {
			return "the stack and the projects we follow";
		}

		if( _shown.size() == 1 ) {
			final Repo only = _shown.iterator().next();
			return only.emoji() + " " + only.name() + " only";
		}

		if( _shown.size() == Repos.repos().size() ) {
			return "all " + _shown.size() + " repositories";
		}

		return _shown.size() + " of " + Repos.repos().size() + " repositories";
	}

	/**
	 * Clicking a repo's row toggles it in or out of the feed. The last shown repo can't be toggled off (an empty feed helps nobody).
	 */
	public NGActionResults toggleRepo() {
		if( _shown.contains( currentRepo ) ) {
			if( _shown.size() > 1 ) {
				_shown.remove( currentRepo );
			}
		}
		else {
			_shown.add( currentRepo );
		}

		return null;
	}

	/**
	 * The row's "only" button: narrow the feed to this one repo
	 */
	public NGActionResults onlyRepo() {
		_shown.clear();
		_shown.add( currentRepo );
		return null;
	}

	/**
	 * Clicking a group's heading shows that group's repos and nothing else
	 */
	public NGActionResults selectGroup() {
		_shown.clear();
		_shown.addAll( currentGroup.repos() );
		return null;
	}

	/**
	 * The card's "all" button: every repository, including the ones left out by default
	 */
	public NGActionResults showAll() {
		_shown.clear();
		_shown.addAll( Repos.repos() );
		return null;
	}

	public NGActionResults clearFilter() {
		_shown.clear();
		_shown.addAll( defaultRepos() );
		return null;
	}

	public boolean isCurrentRepoShown() {
		return _shown.contains( currentRepo );
	}

	public String repoRowClass() {
		return isCurrentRepoShown() ? "is-on" : "is-off";
	}

	/**
	 * @return The marker glyph: a filled dot for a repo in the feed, a hollow one for a repo left out
	 */
	public String repoMarker() {
		return isCurrentRepoShown() ? "●" : "○";
	}

	/**
	 * @return The rail count for the current tab: commits in the last seven days (the fetched total is capped at 50, so it would
	 *         just say 50), releases held, or open issues on GitHub
	 */
	public int currentRepoCount() {
		return switch( tab ) {
			case all, commits -> {
				final Instant weekAgo = Instant.now().minus( 7, ChronoUnit.DAYS );
				yield (int)GithubFeed.shared.commitsFor( currentRepo ).stream().filter( c -> c.committedAt().isAfter( weekAgo ) ).count();
			}
			case releases -> GithubFeed.shared.releasesFor( currentRepo ).size();
			case issues -> GithubFeed.shared.openIssueCountFor( currentRepo );
		};
	}
}
