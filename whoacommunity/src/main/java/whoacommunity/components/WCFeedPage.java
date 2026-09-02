package whoacommunity.components;

import java.util.List;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * Development activity: every commit we hold across the tracked repos,
 * filterable by repository from the rail.
 */
public class WCFeedPage extends WCComponent {

	/**
	 * The filter rail groups repos as ours (undur, ng) and the ones we merely follow
	 */
	public record RepoGroup( String name, List<Repo> repos ) {}

	public RepoGroup currentGroup;
	public Repo currentRepo;

	/**
	 * The repo the user has filtered the commit list down to, or null when
	 * showing all commits.
	 */
	public Repo selectedRepo;

	public WCFeedPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "activity";
	}

	@Override
	public String breadcrumbLeaf() {
		return "activity";
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

		if( selectedRepo == null ) {
			return all;
		}

		return all.stream().filter( c -> c.repo() == selectedRepo ).toList();
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

	public int currentRepoCommitCount() {
		return GithubFeed.shared.commitsFor( currentRepo ).size();
	}
}
