package whoacommunity.data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.MutableAttributes;

import jambalaya.interfaces.DateTimeStamped;
import jambalaya.interfaces.UUIDStamped;
import whoacommunity.data.auto._Comment;

/**
 * A reader's comment on an article. Stored as the Markdown the reader typed;
 * rendered with raw HTML escaped and link targets restricted, since unlike
 * articles this is untrusted input.
 */
public class Comment extends _Comment implements DateTimeStamped, UUIDStamped {

	private static final Parser PARSER;
	private static final HtmlRenderer RENDERER;

	static {
		final MutableDataSet options = new MutableDataSet();
		options.set( Parser.EXTENSIONS, List.of( TablesExtension.create() ) );
		options.set( HtmlRenderer.ESCAPE_HTML, true );
		options.set( HtmlRenderer.SOFT_BREAK, "<br />\n" );

		PARSER = Parser.builder( options ).build();
		RENDERER = HtmlRenderer.builder( options )
				.attributeProviderFactory( new IndependentAttributeProviderFactory() {
					@Override
					public AttributeProvider apply( LinkResolverContext context ) {
						return Comment::sanitizeAttributes;
					}
				} )
				.build();
	}

	public String contentAsHTML() {
		final Node document = PARSER.parse( content() );
		return RENDERER.render( document );
	}

	public String formattedDateTime() {
		return dateTime().format( DateTimeFormatter.ofPattern( "MMMM d, yyyy 'at' HH:mm" ) );
	}

	/**
	 * Drop link/image targets that aren't plain web URLs (javascript:, data:, …)
	 * and mark outbound links as nofollow so comment spam earns nothing.
	 */
	private static void sanitizeAttributes( Node node, AttributablePart part, MutableAttributes attributes ) {
		if( node instanceof Link ) {
			if( !isSafeURL( attributes.getValue( "href" ) ) ) {
				attributes.remove( "href" );
			}
			attributes.replaceValue( "rel", "nofollow noopener noreferrer" );
		}
		else if( node instanceof Image ) {
			if( !isSafeURL( attributes.getValue( "src" ) ) ) {
				attributes.remove( "src" );
			}
		}
	}

	private static boolean isSafeURL( String url ) {
		if( url == null ) {
			return false;
		}

		final String lower = url.strip().toLowerCase( Locale.ROOT );
		return lower.startsWith( "http://" ) || lower.startsWith( "https://" ) || lower.startsWith( "mailto:" );
	}
}
