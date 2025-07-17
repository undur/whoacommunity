package whoacommunity.data;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import jambalaya.interfaces.DateTimeStamped;
import jambalaya.interfaces.UUIDStamped;
import whoacommunity.data.auto._Article;

public class Article extends _Article implements DateTimeStamped, UUIDStamped {

	/**
	 * FIXME: Nicer enum names, following naming conventions, with the stored DB value as a code field 	// Hugi 2025-07-06
	 */
	public enum ArticleFormat {
		html,
		markdown;

		public String code() {
			return toString();
		}
	}

	@Override
	protected void onPostAdd() {
		setPublished( false );
		setFormatCode( ArticleFormat.markdown.toString() );
	}

	/**
	 * @return the article's content formatted as HTML
	 */
	public String contentAsHTML() {
		return switch( format() ) {
			case html -> content();
			case markdown -> renderMarkdown( content() );
		};
	}

	/**
	 * @return The given markdown string rendered as HTML
	 */
	private static String renderMarkdown( String markdownString ) {
		final MutableDataSet options = new MutableDataSet();
		options.set( Parser.EXTENSIONS, List.of( TablesExtension.create(), AsideExtension.create() ) );

		final Parser parser = Parser.builder( options ).build();
		final HtmlRenderer renderer = HtmlRenderer.builder( options ).build();
		final Node document = parser.parse( markdownString );
		return renderer.render( document );
	}

	public String formattedDate() {
		return date().format( DateTimeFormatter.ofPattern( "MMMM d, YYYY" ) );
	}

	public ArticleFormat format() {
		return ArticleFormat.valueOf( formatCode() );
	}

	public void setFormat( ArticleFormat value ) {
		setFormatCode( value.code() );
	}
}