package whoacommunity.components;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

/**
 * The page frame: sidebar with brand and site tree, main column with
 * breadcrumb bar, content and footer. Page identity (breadcrumbs, current
 * section) is read from the page component being wrapped.
 */
public class WCLook extends WCComponent {

	public String searchString;

	public WCLook( NGContext context ) {
		super( context );
	}

	/**
	 * FIXME: This should be configurable. Should be null for a non-fluid layout
	 */
	/**
	 * Stylesheet URLs carry a stamp derived from the file's contents, so a deploy
	 * that changes the CSS also changes the URL and nobody sees the new markup
	 * with an hour-old cached stylesheet.
	 */
	private static final Map<String, String> STAMPED_URLS = new ConcurrentHashMap<>();

	public String whoaCssURL() {
		return stampedResourceURL( "whoa.css" );
	}

	public String whoaFontsCssURL() {
		return stampedResourceURL( "whoa-fonts.css" );
	}

	private static String stampedResourceURL( final String filename ) {
		return STAMPED_URLS.computeIfAbsent( filename, f -> "/nr/app/" + f + "?v=" + contentStamp( "/ng/app/webserver-resources/" + f ) );
	}

	private static String contentStamp( final String classpathResource ) {
		try( InputStream in = WCLook.class.getResourceAsStream( classpathResource ) ) {
			if( in == null ) {
				return "0";
			}

			final CRC32 crc = new CRC32();
			crc.update( in.readAllBytes() );
			return Long.toHexString( crc.getValue() );
		}
		catch( IOException e ) {
			throw new UncheckedIOException( e );
		}
	}

	public String bodyClass() {
		return "layout-fluid";
	}

	public String envString() {
		return "Góður kóði slf. 2026";
	}

	public NGActionResults search() {
		final WCSearchPage nextPage = pageWithName( WCSearchPage.class );
		nextPage.searchString = searchString;
		nextPage.search();
		return nextPage;
	}

}
