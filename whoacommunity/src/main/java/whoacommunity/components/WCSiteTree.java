package whoacommunity.components;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.ObjectContext;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.app.WCCore;
import whoacommunity.components.admin.WCArticleEditPage;
import whoacommunity.data.Article;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.Release;
import whoacommunity.util.Repos;
import whoacommunity.util.Repos.Repo;
import whoacommunity.util.Videos;

/**
 * The persistent site tree in the left sidebar. Fixed order: site sections,
 * then projects (only the current one expanded), then external links.
 * Which item is current comes from the page component's identity methods.
 */
public class WCSiteTree extends WCComponent {

	public record Section( String id, String glyph, String title, String url, String count ) {

		public boolean hasCount() {
			return count != null && !count.isBlank();
		}
	}

	public record SubPage( String id, String title, String url, String count ) {

		public boolean hasCount() {
			return count != null && !count.isBlank();
		}
	}

	public Section section;
	public Repo project;
	public SubPage subPage;

	public WCSiteTree( NGContext context ) {
		super( context );
	}

	public List<Section> sections() {
		final int videoCount = Videos.playlists().stream().mapToInt( p -> p.videos().size() ).sum();

		return List.of(
				new Section( "writing", "◆", "Writing", "/", String.valueOf( articleCount() ) ),
				new Section( "activity", "◈", "Activity", "/dev-feed", null ),
				new Section( "deployment", "▤", "Deployment guides", "/deployment", null ),
				new Section( "videos", "◇", "Videos", "/videos", String.valueOf( videoCount ) ) );
	}

	public String sectionClass() {
		final WCComponent page = page();
		return page != null && section.id().equals( page.pageIdentifier() ) ? "is-current" : "";
	}

	public List<Repo> projects() {
		return Repos.projectRepos();
	}

	/**
	 * The "Overview" row at the top of the Projects section, current on /projects itself
	 */
	public String projectsOverviewClass() {
		final WCComponent page = page();
		return page != null && "projects".equals( page.pageIdentifier() ) ? "is-current" : "";
	}

	public String projectURL() {
		return "/project/" + project.name();
	}

	public boolean isCurrentProject() {
		final WCComponent page = page();
		return page != null && page.currentProjectRepo() == project;
	}

	public String projectClass() {
		return isCurrentProject() ? "is-current" : "";
	}

	/**
	 * @return The latest release tag for the project, or null (an empty pill would look broken)
	 */
	public String projectLatestVersion() {
		final Release latest = GithubFeed.shared.latestReleaseFor( project );
		return latest == null ? null : latest.tagName();
	}

	public boolean projectHasVersion() {
		return projectLatestVersion() != null;
	}

	public List<SubPage> subPages() {
		final List<SubPage> out = new ArrayList<>();
		final String base = projectURL();
		out.add( new SubPage( "overview", "Overview", base, null ) );
		out.add( new SubPage( "commits", "Commits", base + "/commits", countOrNull( GithubFeed.shared.commitsFor( project ).size() ) ) );
		out.add( new SubPage( "releases", "Releases", base + "/releases", countOrNull( GithubFeed.shared.releasesFor( project ).size() ) ) );
		out.add( new SubPage( "issues", "Issues", base + "/issues", countOrNull( GithubFeed.shared.openIssueCountFor( project ) ) ) );
		return out;
	}

	public String subPageClass() {
		final WCComponent page = page();
		final boolean current = page != null && subPage.id().equals( page.currentProjectSubPage() );
		return current ? "is-sub is-current" : "is-sub";
	}

	/**
	 * Admin: create a new (unpublished) article and open it in the editor
	 */
	public NGActionResults createArticle() {
		final ObjectContext oc = WCCore.newContext();

		final Article article = oc.newObject( Article.class );
		article.setDate( LocalDate.now() );
		article.setTitle( "New article" );
		article.setContent( "" );

		oc.commitChanges();

		final WCArticleEditPage page = pageWithName( WCArticleEditPage.class );
		page.selectedObject = article;
		return page;
	}

	private static String countOrNull( final int count ) {
		return count > 0 ? String.valueOf( count ) : null;
	}
}
