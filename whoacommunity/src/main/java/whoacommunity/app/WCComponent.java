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
import whoacommunity.util.Repos;
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
	// ---- Page metadata: title, description, canonical URL (rendered in WCLook's head) ----

	public static final String SITE_NAME = "whoacommunity";
	public static final String SITE_BASE_URL = "https://www.whoacommunity.com";
	public static final String SITE_DESCRIPTION = "Articles, guides and project activity for WebObjects developers — and for what comes after: ng-objects.";

	/**
	 * @return The page's own title, without the site name; pages with real content override this
	 */
	public String pageTitle() {
		final String leaf = breadcrumbLeaf();
		return leaf == null || leaf.isBlank() ? null : leaf;
	}

	/**
	 * @return What goes in the title tag: "<page> · whoacommunity", or the site's own line on the front page
	 */
	public String documentTitle() {
		final String title = pageTitle();
		return title == null ? SITE_NAME + " — WebObjects and ng-objects" : title + " · " + SITE_NAME;
	}

	/**
	 * @return The meta description; pages with real content override this with their own
	 */
	public String pageDescription() {
		return SITE_DESCRIPTION;
	}

	/**
	 * @return The canonical URL for this page: the request path on the www host, without any query string
	 */
	public String canonicalURL() {
		final String uri = context().request().uri();
		final String path = uri == null ? "/" : uri.split( "\\?" )[ 0 ];
		return SITE_BASE_URL + path;
	}

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
		final List<Commit> all = GithubFeed.shared.commits().stream().filter( c -> Repos.inStreams( c.repo() ) ).toList();
		return all.subList( 0, Math.min( 8, all.size() ) );
	}

	public OpenIssue currentIssue;
	public Release currentRelease;

	public List<OpenIssue> openIssues() {
		final List<OpenIssue> all = GithubFeed.shared.issues().stream().filter( i -> Repos.inStreams( i.repo() ) ).toList();
		return all.subList( 0, Math.min( 6, all.size() ) );
	}

	public List<Release> releases() {
		final List<Release> all = GithubFeed.shared.releases().stream().filter( r -> Repos.inStreams( r.repo() ) ).toList();
		return all.subList( 0, Math.min( 6, all.size() ) );
	}
}
