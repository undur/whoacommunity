package whoacommunity.components;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.components.admin.WCArticleEditPage;
import whoacommunity.data.Article;
import whoacommunity.github.Commit;
import whoacommunity.github.GithubFeed;
import whoacommunity.github.OpenIssue;
import whoacommunity.github.Release;
import whoacommunity.util.Repos;

/**
 * The front page: status strip, the lead article in full, the rest as
 * teasers, and the activity rail.
 */
public class WCMain extends WCComponent {

	private static final int TEASER_COUNT = 8;

	public WCMain( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "writing";
	}

	@Override
	public String breadcrumbLeaf() {
		return "writing";
	}

	// ---- Writing ----

	public Article leadArticle() {
		final List<Article> all = articles();
		return all.isEmpty() ? null : all.get( 0 );
	}

	public boolean hasLeadArticle() {
		return leadArticle() != null;
	}

	public String leadArticleURL() {
		return "/article/%s".formatted( leadArticle().uniqueID() );
	}

	public String leadArticleCommentsURL() {
		return leadArticleURL() + "#comments";
	}

	public List<Article> olderArticles() {
		final List<Article> all = articles();
		return all.size() <= 1 ? List.of() : all.subList( 1, Math.min( 1 + TEASER_COUNT, all.size() ) );
	}

	public NGActionResults editArticle() {
		final WCArticleEditPage nextPage = pageWithName( WCArticleEditPage.class );
		nextPage.selectedObject = leadArticle();
		return nextPage;
	}

	// ---- Status strip ----

	public Release latestRelease() {
		return GithubFeed.shared.releases().stream().filter( r -> Repos.inStreams( r.repo() ) ).findFirst().orElse( null );
	}

	public boolean hasLatestRelease() {
		return latestRelease() != null;
	}

	private Instant weekAgo() {
		return Instant.now().minus( 7, ChronoUnit.DAYS );
	}

	/**
	 * Commits in the last week to our own repos (undur, ng) — the ones we follow but don't own don't count
	 */
	private List<Commit> ourCommitsThisWeek() {
		final Instant since = weekAgo();
		return GithubFeed.shared.commits().stream()
				.filter( c -> Repos.isOurs( c.repo() ) )
				.filter( c -> c.committedAt() != null && c.committedAt().isAfter( since ) )
				.toList();
	}

	/**
	 * @return The latest release on its project's releases page, anchored — or the activity releases tab for a repo without a project page
	 */
	public String latestReleaseURL() {
		final Release release = latestRelease();

		if( Repos.projectRepoNamed( release.repo().name() ).isPresent() ) {
			return "/project/" + release.repo().name() + "/releases#" + release.anchorId();
		}

		return "/dev-feed?tab=releases";
	}

	public long commitCountThisWeek() {
		return ourCommitsThisWeek().size();
	}

	public long activeRepoCount() {
		return ourCommitsThisWeek().stream().map( Commit::repo ).distinct().count();
	}

	public String activeRepoCountString() {
		final long n = activeRepoCount();
		return n == 1 ? "across 1 of our repositories" : "across %d of our repositories".formatted( n );
	}

	public int openIssueCount() {
		return GithubFeed.shared.openIssueCountTotal();
	}

	/**
	 * @return "3 opened in September" — counting the issues we actually hold (the most recently updated per repo)
	 */
	public String issuesOpenedThisMonthString() {
		final LocalDate today = LocalDate.now();
		final Instant monthStart = today.withDayOfMonth( 1 ).atStartOfDay( ZoneId.systemDefault() ).toInstant();
		final long n = GithubFeed.shared.issues().stream().map( OpenIssue::createdAt ).filter( d -> d != null && d.isAfter( monthStart ) ).count();
		final String month = today.getMonth().getDisplayName( TextStyle.FULL, Locale.ENGLISH );
		return "%d opened in %s".formatted( n, month );
	}
}
