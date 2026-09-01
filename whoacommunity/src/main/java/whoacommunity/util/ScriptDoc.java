package whoacommunity.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A shell script parsed into a literate two-column document: section banners
 * (### … ###) and code chunks, with block comments as each chunk's
 * explanation. Extracted from WCDeploymentPage so every deployment-flavor
 * page renders the same way from its own bundled script. Because the pages
 * are parsed from the actual scripts at runtime they can't drift from what
 * the scripts do.
 */
public record ScriptDoc( List<String> intro, List<Step> steps, String fullScript ) {

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

	public static ScriptDoc parse( final String resourcePath ) {

		final String fullScript;

		try( InputStream in = ScriptDoc.class.getResourceAsStream( resourcePath ) ) {
			fullScript = new String( in.readAllBytes(), StandardCharsets.UTF_8 );
		}
		catch( final IOException | NullPointerException e ) {
			throw new IllegalStateException( resourcePath + " missing from the bundle", e );
		}

		final List<String> intro = new ArrayList<>();
		final List<Step> steps = new ArrayList<>();
		final List<String> annotation = new ArrayList<>();
		final List<String> code = new ArrayList<>();
		boolean inIntro = true;
		String heredocTerminator = null;

		for( final String line : fullScript.split( "\n", -1 ) ) {
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
				flush( steps, annotation, code );
				code.add( line );
				flush( steps, annotation, code );
				inIntro = false;
				continue;
			}

			// Comment line (but not the shebang): explanation column.
			// The leading header block becomes the page intro instead.
			if( stripped.startsWith( "#" ) && !stripped.startsWith( "#!" ) ) {
				if( !code.isEmpty() ) {
					flush( steps, annotation, code );
				}
				final String text = stripped.replaceFirst( "^#\\s?", "" );
				if( inIntro ) {
					intro.add( text );
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
		flush( steps, annotation, code );

		return new ScriptDoc( List.copyOf( intro ), List.copyOf( steps ), fullScript );
	}

	private static void flush( final List<Step> steps, final List<String> annotation, final List<String> code ) {
		while( !code.isEmpty() && code.getLast().isBlank() ) {
			code.removeLast();
		}
		if( annotation.isEmpty() && code.isEmpty() ) {
			return;
		}
		steps.add( new Step( paragraphs( annotation ), String.join( "\n", code ) ) );
		annotation.clear();
		code.clear();
	}

	/**
	 * Blank comment lines separate paragraphs; within one, lines join with a
	 * space — flowing prose that wraps to its column.
	 */
	private static List<String> paragraphs( final List<String> commentLines ) {
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
					current.append( " " );
				}
				current.append( line );
			}
		}
		if( !current.isEmpty() ) {
			result.add( current.toString() );
		}
		return List.copyOf( result );
	}
}
