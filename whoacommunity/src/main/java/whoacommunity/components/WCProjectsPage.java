package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.Release;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;

/**
 * The projects overview: one card per project with emoji, name and a blurb.
 */
public class WCProjectsPage extends WCComponent {

	public Repo currentProject;

	public WCProjectsPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "projects";
	}

	@Override
	public String breadcrumbLeaf() {
		return "projects";
	}

	public List<Repo> projects() {
		return Repos.projectRepos();
	}

	public String currentProjectURL() {
		return "/project/" + currentProject.name();
	}

	/**
	 * @return Our own blurb, or GitHub's description (minus its leading emoji) when we haven't written one
	 */
	public String currentProjectBlurb() {
		final String blurb = Repos.blurbFor( currentProject );

		if( blurb != null ) {
			return blurb;
		}

		final String description = GithubFeed.shared.descriptionFor( currentProject );
		return description == null ? "" : description.replaceFirst( "^[\\p{So}\\p{Sk}\\p{Mn}\\p{Cf}\\p{Cs}\\s\\-–—·:]+", "" ).strip();
	}

	public String currentProjectVersion() {
		final Release latest = GithubFeed.shared.latestReleaseFor( currentProject );
		return latest == null ? null : latest.tagName();
	}

	public boolean currentProjectHasVersion() {
		return currentProjectVersion() != null;
	}
}
