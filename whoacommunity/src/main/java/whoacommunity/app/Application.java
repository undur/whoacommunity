package whoacommunity.app;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.cayenne.query.ObjectSelect;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

import ng.appserver.NGActionResults;
import ng.appserver.NGApplication;
import ng.appserver.NGRequest;
import ng.appserver.NGResponse;
import ng.appserver.NGSessionRestorationException;
import ng.plugins.Elements;
import ng.plugins.Routes;
import whoacommunity.components.WCArticleDetailPage;
import whoacommunity.components.WCDeploymentApacheClassicPage;
import whoacommunity.components.WCDeploymentApacheModuloPage;
import whoacommunity.components.WCDeploymentPage;
import whoacommunity.components.WCFeedPage;
import whoacommunity.components.WCMain;
import whoacommunity.components.WCSlackArchivePage;
import whoacommunity.components.WCSlackClientPage;
import whoacommunity.components.WCVideoDetailPage;
import whoacommunity.components.WCVideosPage;
import whoacommunity.data.Article;
import whoacommunity.github.GithubFeed;
import whoacommunity.util.CachedFeed;
import whoacommunity.util.Videos;
import whoacommunity.util.Videos.Video;

public class Application extends NGApplication {

	static {
		// FIXME: We really need to support a nicer way to set and change java properties (at least WRT logging) // Hugi 2025-09-21
		System.setProperty( "org.slf4j.simpleLogger.log.cayenne-sql", "warn" );
	}

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		// Feeds register with CachedFeed when their holding class loads, which
		// normally happens lazily on first page render. Load them now so the
		// boot-time sweep below warms every cache before the first visitor.
		GithubFeed.shared.getClass();

		try {
			Class.forName( "whoacommunity.components.WCSidebar" );
		}
		catch( ClassNotFoundException e ) {
			throw new RuntimeException( e );
		}

		// Keep all caches fresh off the request path: sweep for expired TTLs
		// once a minute (each feed still refreshes at its own cadence).
		CachedFeed.startBackgroundRefresh( Duration.ofMinutes( 1 ) );
	}

	@Override
	public Elements elements() {
		return Elements
				.create()
				.elementPackage( "whoacommunity.components" )
				.elementPackage( "whoacommunity.components.admin" );
	}

	/**
	 * On session timeout, send the user back to where they were instead of
	 * the default "session timed out" page. URLs containing /wo/ are
	 * component-action URLs that embed session-specific element IDs and
	 * can't be safely re-dispatched, so we fall back to "/" for those.
	 */
	@Override
	public NGActionResults responseForSessionRestorationException( final NGSessionRestorationException exception ) {
		final String urlToReturnTo = exception.request().uri();

		if( urlToReturnTo != null && !urlToReturnTo.contains( "/no/" ) ) {
			return resetSessionCookieWithRedirectToURL( urlToReturnTo );
		}

		return resetSessionCookieWithRedirectToURL( "/" );
	}

	/**
	 * The framework serves CSS resources as "text/css" with no charset, so browsers guess the encoding —
	 * Firefox falls back to Latin-1 and mangles our UTF-8 stylesheet (em-dashes, arrows, etc.). Patch the
	 * Content-Type on the way out to pin UTF-8. Drop this once the framework lets us set resource charsets.
	 *
	 * FIXME: This needs resolution in ng // Hugi 2026-05-26
	 */
	@Override
	public NGResponse dispatchRequest( final NGRequest request ) {
		final NGResponse response = super.dispatchRequest( request );

		if( "text/css".equals( response.headerForKey( "Content-Type" ) ) ) {
			response.setHeader( "Content-Type", "text/css; charset=utf-8" );
		}

		// Font files never change (a changed font gets a new filename), so let
		// browsers cache them for a year instead of the default hour.
		if( request.uri().endsWith( ".woff2" ) ) {
			response.setHeader( "Cache-Control", "public, max-age=31536000, immutable" );
		}

		return response;
	}

	@Override
	public Routes routes() {
		return Routes
				.create()
				.map( "/", WCMain.class )
				.map( "/slack-archive", WCSlackArchivePage.class )
				.map( "/slack-client", WCSlackClientPage.class )
				.map( "/dev-feed", WCFeedPage.class )
				.map( "/article/*", this::viewArticle )
				.map( "/videos", WCVideosPage.class )
				.map( "/video/*", this::viewVideo )
				.map( "/atom.xml", this::atom )
				.map( "/refresh-data", this::refreshData )
				.map( "/deployment-config", WCDeploymentPage.class )
				.map( "/deployment-apache-modulo", WCDeploymentApacheModuloPage.class )
				.map( "/deployment-apache-mod-webobjects", WCDeploymentApacheClassicPage.class );
	}

	/**
	 * Force-refresh every cached feed (GitHub + Java RSS) regardless of TTL,
	 * then redirect home. Handy after publishing an article or pushing
	 * commits you want surfaced immediately.
	 */
	public NGActionResults refreshData( NGRequest request ) {
		CachedFeed.forceRefreshAll();
		final NGResponse response = new NGResponse( "", 302 );
		response.setHeader( "Location", "/" );
		return response;
	}

	/**
	 * @return Our atom feed.
	 *
	 * FIXME: Butt ugly test. Clean up // Hugi 2025-10-26
	 */
	public NGActionResults atom( NGRequest request ) {
		final SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType( "atom_1.0" );
		feed.setTitle( "whoacommunity.com articles" );
		feed.setLink( "https://www.whoacommunity.com/atom.xml" );
		feed.setDescription( "Articles from whoacommunity.com" );
		feed.setPublishedDate( new Date() );

		final List<SyndEntry> entries = new ArrayList<>();

		final ObjectSelect<Article> query = ObjectSelect
				.query( Article.class )
				.where( Article.PUBLISHED.isTrue() )
				.orderBy( Article.DATE.desc() );

		for( Article article : query.select( WCCore.newContext() ) ) {
			SyndEntry entry = new SyndEntryImpl();
			entry.setTitle( article.title() );
			entry.setLink( "https://www.whoacommunity.com/article/" + article.uniqueID() );
			entry.setPublishedDate( Date.from( article.date().atStartOfDay( ZoneId.systemDefault() ).toInstant() ) );
			entries.add( entry );
		}

		feed.setEntries( entries );

		try {
			final String xmlString = new SyndFeedOutput().outputString( feed );
			final NGResponse response = new NGResponse( xmlString, 200 );
			response.setHeader( "content-type", "application/atom+xml" );
			return response;
		}
		catch( FeedException e ) {
			throw new RuntimeException( e );
		}
	}

	public NGActionResults viewVideo( NGRequest request ) {
		final String youtubeID = request.parsedURI().getString( 1 );
		final Video video = Videos.videoWithID( youtubeID ).orElse( null );

		if( video == null ) {
			return new NGResponse( "No such video", 404 );
		}

		final WCVideoDetailPage page = pageWithName( WCVideoDetailPage.class, request.context() );
		page.video = video;
		return page;
	}

	public NGActionResults viewArticle( NGRequest request ) {
		final String uuidString = request.parsedURI().getString( 1 );
		final UUID uuid = UUID.fromString( uuidString );

		final Article article = ObjectSelect
				.query( Article.class )
				.where( Article.UNIQUE_ID.eq( uuid ) )
				.selectOne( WCCore.newContext() );

		final WCArticleDetailPage page = pageWithName( WCArticleDetailPage.class, request.context() );
		page.selectedObject = article;
		return page;
	}
}