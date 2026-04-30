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

	public String currentArticleURL() {
		return "/article/%s".formatted( currentArticle.uniqueID() );
	}

	public List<Commit> items() {
		final List<Commit> all = GithubFeed.shared.commits();
		return all.subList( 0, Math.min( 10, all.size() ) );
	}

	public OpenIssue currentIssue;
	public Release currentRelease;

	public List<OpenIssue> openIssues() {
		final List<OpenIssue> all = GithubFeed.shared.issues();
		return all.subList( 0, Math.min( 10, all.size() ) );
	}

	public List<Release> releases() {
		final List<Release> all = GithubFeed.shared.releases();
		return all.subList( 0, Math.min( 10, all.size() ) );
	}
}