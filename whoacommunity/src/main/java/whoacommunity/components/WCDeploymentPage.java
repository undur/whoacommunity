package whoacommunity.components;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;

/**
 * The full-stack setup guide, rendered from the actual setup-server.sh as
 * a continuous two-column document: the script reads straight down the
 * left column — section banners, inline comments and all — while its
 * block comments become the explanations on the right. Because the page
 * is parsed from the bundled script at runtime it can't drift from what
 * the script does, and because the explanation column is unselectable,
 * selecting and copying the page yields a runnable script (the copy
 * button hands over the verbatim file, comments included).
 */
public class WCDeploymentPage extends NGComponent {

	/** One row: a chunk of script and the comment block that described it. */
	public record Step( List<String> annotationParagraphs, String code ) {

		/** Section banner rows (### … ###) get the separator line above them. */
		public boolean isBanner() {
			return code.startsWith( "###" );
		}

		/** The banner's title, shorn of its # decoration — shown in the explanation column. */
		public String bannerTitle() {
			return code.replaceAll( "^#+\\s*|\\s*#+$", "" );
		}

		/**
		 * The chunk as HTML with comments wrapped in a span — the one place
		 * we render markup ourselves (escaping first), so the template can
		 * color comments without a syntax highlighter.
		 */
		public String codeHtml() {
			final StringBuilder html = new StringBuilder();
			for( final String line : code.split( "\n", -1 ) ) {
				if( !html.isEmpty() ) {
					html.append( '\n' );
				}
				int commentStart = -1;
				if( line.stripLeading().startsWith( "#" ) ) {
					commentStart = line.indexOf( '#' );
				}
				else {
					final int inline = line.indexOf( " #" );
					if( inline >= 0 ) {
						commentStart = inline + 1;
					}
				}
				if( commentStart < 0 ) {
					html.append( escape( line ) );
				}
				else {
					html.append( escape( line.substring( 0, commentStart ) ) )
							.append( "<span class=\"cmt\">" )
							.append( escape( line.substring( commentStart ) ) )
							.append( "</span>" );
				}
			}
			return html.toString();
		}

		private static String escape( final String s ) {
			return s.replace( "&", "&amp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" );
		}
	}

	private static final List<String> _intro = new ArrayList<>();
	private static final List<Step> _steps = new ArrayList<>();
	private static String _fullScript;

	static {
		parseScript();
	}

	public Step currentStep;
	public String currentParagraph;

	public WCDeploymentPage( NGContext context ) {
		super( context );
	}

	public List<String> intro() {
		return _intro;
	}

	public List<Step> steps() {
		return _steps;
	}

	/** The verbatim script, for the copy button. */
	public String fullScript() {
		return _fullScript;
	}

	private static void parseScript() {
		// Idempotent on purpose: under DCEVM/HotswapAgent a class redefinition
		// can re-run this against already-populated statics
		_intro.clear();
		_steps.clear();

		try( InputStream in = WCDeploymentPage.class.getResourceAsStream( "/setup-server.sh" ) ) {
			_fullScript = new String( in.readAllBytes(), StandardCharsets.UTF_8 );
		}
		catch( final IOException | NullPointerException e ) {
			throw new IllegalStateException( "setup-server.sh missing from the bundle", e );
		}

		final List<String> annotation = new ArrayList<>();
		final List<String> code = new ArrayList<>();
		boolean inIntro = true;
		String heredocTerminator = null;

		for( final String line : _fullScript.split( "\n", -1 ) ) {
			final String stripped = line.strip();

			// Inside a heredoc everything is payload — comments included
			if( heredocTerminator != null ) {
				code.add( line );
				if( stripped.equals( heredocTerminator ) ) {
					heredocTerminator = null;
				}
				continue;
			}

			// Section banners stay in the script flow, as their own row
			if( stripped.startsWith( "### " ) ) {
				flush( annotation, code );
				code.add( line );
				flush( annotation, code );
				inIntro = false;
				continue;
			}

			// Comment line (but not the shebang): explanation column.
			// The leading header block becomes the page intro instead.
			if( stripped.startsWith( "#" ) && !stripped.startsWith( "#!" ) ) {
				if( !code.isEmpty() ) {
					flush( annotation, code );
				}
				final String text = stripped.replaceFirst( "^#\\s?", "" );
				if( inIntro ) {
					_intro.add( text );
				}
				else {
					annotation.add( text );
				}
				continue;
			}

			if( stripped.isEmpty() ) {
				if( !code.isEmpty() ) {
					code.add( line );
				}
				continue;
			}

			// Code. The shebang is code (the copyable flow starts with it)
			// but doesn't end the intro — the header block follows it.
			if( !stripped.startsWith( "#!" ) ) {
				inIntro = false;
			}
			code.add( line );

			final Matcher heredoc = Pattern.compile( "<<\\s*[\"']?(\\w+)[\"']?" ).matcher( line );
			if( heredoc.find() ) {
				heredocTerminator = heredoc.group( 1 );
			}
		}
		flush( annotation, code );
	}

	private static void flush( final List<String> annotation, final List<String> code ) {
		while( !code.isEmpty() && code.getLast().isBlank() ) {
			code.removeLast();
		}
		if( annotation.isEmpty() && code.isEmpty() ) {
			return;
		}
		_steps.add( new Step( paragraphs( annotation, " " ), String.join( "\n", code ) ) );
		annotation.clear();
		code.clear();
	}

	/**
	 * Blank comment lines separate paragraphs; within one, lines join with
	 * [separator] — a space for explanations (flowing prose that wraps to
	 * its column) vs a newline where source line structure matters.
	 */
	private static List<String> paragraphs( final List<String> commentLines, final String separator ) {
		final List<String> result = new ArrayList<>();
		final StringBuilder current = new StringBuilder();
		for( final String line : commentLines ) {
			if( line.isBlank() ) {
				if( !current.isEmpty() ) {
					result.add( current.toString() );
					current.setLength( 0 );
				}
			}
			else {
				if( !current.isEmpty() ) {
					current.append( separator );
				}
				current.append( line );
			}
		}
		if( !current.isEmpty() ) {
			result.add( current.toString() );
		}
		return result;
	}
}
