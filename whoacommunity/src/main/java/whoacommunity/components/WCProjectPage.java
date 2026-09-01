package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.OpenIssue;
import whoacommunity.github.Release;
import whoacommunity.util.Markdown;
import whoacommunity.util.Repos.Repo;

/**
 * First cut of a project page: one repo, its README and its recent activity.
 * A "project" will eventually be more than one repo and carry its own content.
 */
public class WCProjectPage extends WCComponent {

	public Repo repo;

	public WCProjectPage( NGContext context ) {
		super( context );
	}

	public String readmeAsHTML() {
		final String readme = GithubFeed.shared.readmeFor( repo );
		return readme == null ? null : Markdown.render( readme );
	}

	public List<Commit> projectCommits() {
		final List<Commit> all = GithubFeed.shared.commitsFor( repo );
		return all.subList( 0, Math.min( 15, all.size() ) );
	}

	public List<Release> projectReleases() {
		return GithubFeed.shared.releasesFor( repo );
	}

	public List<OpenIssue> projectIssues() {
		return GithubFeed.shared.issuesFor( repo );
	}
}
