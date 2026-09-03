package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.data.Article;

/**
 * The writing archive: every article, newest first.
 */
public class WCWritingPage extends WCComponent {

	public WCWritingPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "writing";
	}

	/**
	 * @return Published articles only; drafts get their own section
	 */
	public List<Article> publishedArticles() {
		return articles().stream().filter( Article::published ).toList();
	}

	/**
	 * @return Unpublished articles — only ever non-empty when logged in, since articles() hides drafts otherwise
	 */
	public List<Article> draftArticles() {
		return articles().stream().filter( a -> !a.published() ).toList();
	}

	public boolean hasDrafts() {
		return !draftArticles().isEmpty();
	}

	@Override
	public String breadcrumbLeaf() {
		return "writing";
	}
}
