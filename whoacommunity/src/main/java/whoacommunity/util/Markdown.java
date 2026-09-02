package whoacommunity.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Markdown rendering for content we trust (articles, our own repos' READMEs).
 * Untrusted input (comments) has its own hardened renderer in Comment.
 *
 * Headings get stable ids so an "on this page" rail can link to them.
 */
public class Markdown {

	private static final Parser PARSER;
	private static final HtmlRenderer RENDERER;

	private static final Pattern H2 = Pattern.compile( "<h2 id=\"([^\"]+)\">(.*?)</h2>", Pattern.DOTALL );
	private static final Pattern TAGS = Pattern.compile( "<[^>]+>" );

	static {
		final MutableDataSet options = new MutableDataSet();
		options.set( Parser.EXTENSIONS, List.of( TablesExtension.create(), AsideExtension.create() ) );
		options.set( HtmlRenderer.GENERATE_HEADER_ID, true );
		options.set( HtmlRenderer.RENDER_HEADER_ID, true );
		PARSER = Parser.builder( options ).build();
		RENDERER = HtmlRenderer.builder( options ).build();
	}

	public static String render( final String markdown ) {
		final Node document = PARSER.parse( markdown );
		return RENDERER.render( document );
	}

	/**
	 * A second-level heading in rendered HTML, for building an "on this page" list
	 */
	public record Heading( String id, String title ) {}

	/**
	 * @return The h2 headings (id + plain-text title) found in rendered HTML, in document order
	 */
	public static List<Heading> headings( final String html ) {
		final List<Heading> out = new ArrayList<>();

		if( html == null ) {
			return out;
		}

		final Matcher m = H2.matcher( html );

		while( m.find() ) {
			final String title = TAGS.matcher( m.group( 2 ) ).replaceAll( "" ).strip();
			out.add( new Heading( m.group( 1 ), title ) );
		}

		return out;
	}

	/**
	 * @return Rendered HTML reduced to plain text: tags stripped, entities for the common cases decoded, whitespace collapsed
	 */
	public static String plainText( final String html ) {
		if( html == null ) {
			return "";
		}

		return TAGS.matcher( html ).replaceAll( " " )
				.replace( "&amp;", "&" )
				.replace( "&lt;", "<" )
				.replace( "&gt;", ">" )
				.replace( "&quot;", "\"" )
				.replace( "&#39;", "'" )
				.replace( "&nbsp;", " " )
				.replaceAll( "\\s+", " " )
				.strip();
	}
}
