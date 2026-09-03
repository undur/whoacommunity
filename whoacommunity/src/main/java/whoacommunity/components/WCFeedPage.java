package whoacommunity.components;

import java.util.List;

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
	 * The filter rail groups repos as ours (undur, ng) and the ones we merely follow
	 */
	public record RepoGroup( String name, List<Repo> repos ) {}

	public enum Tab {
		commits( "Commits", "50 most recent commits each", "Only the 50 most recent commits are fetched per repository, so this isn't the full history — it's what GitHub's feed gives us." ),
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
	public Tab tab = Tab.commits;

	/**
	 * The repo the user has filtered the list down to, or null when showing everything.
	 */
	public Repo selectedRepo;

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
				new RepoGroup( "Our repositories", Repos.ourRepos() ),
				new RepoGroup( "Others", Repos.otherRepos() ) );
	}

	/**
	 * @return All commits across the tracked repos, optionally filtered by the selected repo
	 */
	public List<Commit> allCommits() {
		final List<Commit> all = GithubFeed.shared.commits();
		return selectedRepo == null ? all : all.stream().filter( c -> c.repo() == selectedRepo ).toList();
	}

	public List<Release> allReleases() {
		final List<Release> all = GithubFeed.shared.releases();
		return selectedRepo == null ? all : all.stream().filter( r -> r.repo() == selectedRepo ).toList();
	}

	public List<OpenIssue> allIssues() {
		final List<OpenIssue> all = GithubFeed.shared.issues();
		return selectedRepo == null ? all : all.stream().filter( i -> i.repo() == selectedRepo ).toList();
	}

	public boolean hasFilter() {
		return selectedRepo != null;
	}

	public String filterDescription() {
		return selectedRepo == null ? "all repositories" : selectedRepo.emoji() + " " + selectedRepo.name();
	}

	public NGActionResults selectRepo() {
		// Clicking the already-selected repo clears the filter
		selectedRepo = ( currentRepo == selectedRepo ) ? null : currentRepo;
		return null;
	}

	public NGActionResults clearFilter() {
		selectedRepo = null;
		return null;
	}

	public String repoFilterClass() {
		return currentRepo == selectedRepo ? "is-on" : "";
	}

	/**
	 * @return How many rows the current tab holds for the rail's repo, so the counts follow the tab
	 */
	public int currentRepoCount() {
		return switch( tab ) {
			case commits -> GithubFeed.shared.commitsFor( currentRepo ).size();
			case releases -> GithubFeed.shared.releasesFor( currentRepo ).size();
			case issues -> GithubFeed.shared.openIssueCountFor( currentRepo );
		};
	}
}
