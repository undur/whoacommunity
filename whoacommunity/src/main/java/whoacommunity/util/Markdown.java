package whoacommunity.util;

import java.util.List;

import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Markdown rendering for content we trust (articles, our own repos' READMEs).
 * Untrusted input (comments) has its own hardened renderer in Comment.
 */
public class Markdown {

	private static final Parser PARSER;
	private static final HtmlRenderer RENDERER;

	static {
		final MutableDataSet options = new MutableDataSet();
		options.set( Parser.EXTENSIONS, List.of( TablesExtension.create(), AsideExtension.create() ) );
		PARSER = Parser.builder( options ).build();
		RENDERER = HtmlRenderer.builder( options ).build();
	}

	public static String render( final String markdown ) {
		final Node document = PARSER.parse( markdown );
		return RENDERER.render( document );
	}
}
