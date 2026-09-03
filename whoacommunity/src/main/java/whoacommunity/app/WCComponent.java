package whoacommunity.app;

import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.data.Article;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.OpenIssue;
import whoacommunity.github.Release;
import whoacommunity.util.Repos.Repo;

public abstract class WCComponent extends NGComponent {

	public WCComponent( NGContext context ) {
		super( context );
	}

	/**
	 * FIXME: A hack to determine whether we show the admin page. Will eventually be controlled through login/access privileges // Hugi 2025-07-06
	 */
	@Deprecated
	public boolean isLocal() {
		return application().isDevelopmentMode();
	}

	public boolean showAdminStuff() {
		return isLocal();
	}

	// ---- Page identity, used by the site tree and the breadcrumb bar ----

	/**
	 * One crumb in the breadcrumb trail. The leaf (current page) is not a crumb; see breadcrumbLeaf().
	 */
	public record Crumb( String title, String url ) {}

	public static final Crumb HOME_CRUMB = new Crumb( "whoacommunity", "/" );

	/**
	 * @return Which site section this page belongs to: "writing", "videos", "activity", "guides", "projects", "project" — or null
	 */
	public String pageIdentifier() {
		return null;
	}

	/**
	 * @return The trail leading to this page, not including the page itself
	 */
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB );
	}

	/**
	 * @return The current page's own name, shown last in the breadcrumb bar (plain text, never a link)
	 */
	public String breadcrumbLeaf() {
		return "";
	}

	/**
	 * @return The project this page is about, if it's a project page (expands that project in the site tree)
	 */
	public Repo currentProjectRepo() {
		return null;
	}

	/**
	 * @return "overview", "commits", "releases" or "issues" on project pages, else null
	 */
	public String currentProjectSubPage() {
		return null;
	}

	/**
	 * @return "modulo", "apache-modulo" or "apache-mod-webobjects" on deployment guide pages, else null
	 */
	public String currentDeploymentGuide() {
		return null;
	}

	/**
	 * @return The page component being rendered, from the context (used by the layout and the site tree)
	 */
	public WCComponent page() {
		final NGComponent page = context().page();
		return page instanceof WCComponent wc ? wc : null;
	}

	/* FIXME: The following should really be in a utility class rather than the shared component superclass */

	public Article currentArticle;
	private List<Article> _articles;
	public Commit current;

	public List<Article> articles() {
		if( _articles == null ) {
			final ObjectSelect<Article> query = ObjectSelect
					.query( Article.class )
					.orderBy( Article.DATE.desc() );

			if( !showAdminStuff() ) {
				query.where( Article.PUBLISHED.isTrue() );
			}

			_articles = query.select( WCCore.newContext() );
		}

		return _articles;
	}

	/**
	 * @return The most recent articles, for the activity rail
	 */
	public List<Article> recentArticles() {
		final List<Article> all = articles();
		return all.subList( 0, Math.min( 8, all.size() ) );
	}

	public int articleCount() {
		return articles().size();
	}

	public String currentArticleURL() {
		return "/article/%s".formatted( currentArticle.uniqueID() );
	}

	public String currentArticleCommentsURL() {
		return currentArticleURL() + "#comments";
	}

	public List<Commit> items() {
		final List<Commit> all = GithubFeed.shared.commits();
		return all.subList( 0, Math.min( 8, all.size() ) );
	}

	public OpenIssue currentIssue;
	public Release currentRelease;

	public List<OpenIssue> openIssues() {
		final List<OpenIssue> all = GithubFeed.shared.issues();
		return all.subList( 0, Math.min( 6, all.size() ) );
	}

	public List<Release> releases() {
		final List<Release> all = GithubFeed.shared.releases();
		return all.subList( 0, Math.min( 6, all.size() ) );
	}
}
