package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.OpenIssue;
import whoacommunity.github.Release;
import whoacommunity.util.Markdown;
import whoacommunity.util.Markdown.Heading;
import whoacommunity.util.Repos.Repo;

/**
 * A project's documentation surface: overview (README, synced from GitHub,
 * with an on-this-page rail) and the Commits / Releases / Issues sub-pages.
 */
public class WCProjectPage extends WCComponent {

	public enum SubPage {
		overview,
		commits,
		releases,
		issues
	}

	public Repo repo;
	public SubPage subPage = SubPage.overview;

	public Heading currentHeading;

	public WCProjectPage( NGContext context ) {
		super( context );
	}

	// ---- Identity for the tree and breadcrumbs ----

	@Override
	public String pageIdentifier() {
		return "project";
	}

	@Override
	public Repo currentProjectRepo() {
		return repo;
	}

	@Override
	public String currentProjectSubPage() {
		return subPage.name();
	}

	@Override
	public List<Crumb> breadcrumbs() {
		if( subPage == SubPage.overview ) {
			return List.of( HOME_CRUMB, new Crumb( "projects", "/" ) );
		}

		return List.of( HOME_CRUMB, new Crumb( "projects", "/" ), new Crumb( repo.name(), projectURL() ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return subPage == SubPage.overview ? repo.name() : subPage.name();
	}

	public String projectURL() {
		return "/project/" + repo.name();
	}

	public String projectCommitsURL() {
		return projectURL() + "/commits";
	}

	public boolean isOverview() {
		return subPage == SubPage.overview;
	}

	public boolean isCommits() {
		return subPage == SubPage.commits;
	}

	public boolean isReleases() {
		return subPage == SubPage.releases;
	}

	public boolean isIssues() {
		return subPage == SubPage.issues;
	}

	// ---- Head: facts strip ----

	/**
	 * @return The repo's GitHub description, minus the emoji most of them open with
	 *         (the header already shows it)
	 */
	public String description() {
		final String description = GithubFeed.shared.descriptionFor( repo );

		if( description == null ) {
			return null;
		}

		String stripped = description.strip();

		if( repo.emoji() != null && stripped.startsWith( repo.emoji() ) ) {
			stripped = stripped.substring( repo.emoji().length() );
		}

		// Any other leading symbols/emoji (incl. variation selectors and ZWJ sequences) and separators
		stripped = stripped.replaceFirst( "^[\\p{So}\\p{Sk}\\p{Mn}\\p{Cf}\\p{Cs}\\s\\-–—·:]+", "" ).strip();

		return stripped.isEmpty() ? null : stripped;
	}

	public Release latestRelease() {
		return GithubFeed.shared.latestReleaseFor( repo );
	}

	public boolean hasLatestRelease() {
		return latestRelease() != null;
	}

	public Commit latestCommit() {
		final List<Commit> all = GithubFeed.shared.commitsFor( repo );
		return all.isEmpty() ? null : all.get( 0 );
	}

	public boolean hasLatestCommit() {
		return latestCommit() != null;
	}

	public String commitsOnGithubURL() {
		return repo.url() + "/commits";
	}

	public String releasesOnGithubURL() {
		return repo.url() + "/releases";
	}

	public String issuesOnGithubURL() {
		return repo.url() + "/issues";
	}

	public String readmeOnGithubURL() {
		return repo.url() + "/blob/HEAD/README.md";
	}

	// ---- Overview ----

	private String _readmeHTML;
	private boolean _readmeRendered;

	public String readmeAsHTML() {
		if( !_readmeRendered ) {
			final String readme = GithubFeed.shared.readmeFor( repo );
			_readmeHTML = readme == null ? null : Markdown.render( readme );
			_readmeRendered = true;
		}

		return _readmeHTML;
	}

	public boolean hasReadme() {
		return readmeAsHTML() != null;
	}

	public List<Heading> headings() {
		return Markdown.headings( readmeAsHTML() );
	}

	public boolean hasHeadings() {
		return !headings().isEmpty();
	}

	public String currentHeadingHref() {
		return "#" + currentHeading.id();
	}

	// ---- Activity ----

	public List<Commit> recentCommits() {
		final List<Commit> all = GithubFeed.shared.commitsFor( repo );
		return all.subList( 0, Math.min( 5, all.size() ) );
	}

	public List<Commit> projectCommits() {
		return GithubFeed.shared.commitsFor( repo );
	}

	public List<Release> projectReleases() {
		return GithubFeed.shared.releasesFor( repo );
	}

	public List<OpenIssue> projectIssues() {
		return GithubFeed.shared.issuesFor( repo );
	}

	public int openIssueCount() {
		return GithubFeed.shared.openIssueCountFor( repo );
	}
}
