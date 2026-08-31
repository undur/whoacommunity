package whoacommunity.components;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;

/**
 * The full-stack setup guide, rendered from the actual setup-server.sh:
 * the script's section markers become headings, its comment blocks become
 * the narrative column, and the code stands beside them untouched (inline
 * comments stay in the code, where Prism styles them). Because the page is
 * parsed from the bundled script at runtime, it can't drift from what the
 * script actually does — updating the guide is copying in a newer script.
 */
public class WCDeploymentPage extends NGComponent {

	/** A step: a comment block and the code it describes, side by side. */
	public record Step( List<String> annotationParagraphs, String code ) {}

	/** A section of the script (### header ###) with its steps. */
	public record Section( String title, List<Step> steps ) {}

	private static final List<String> _intro = new ArrayList<>();
	private static final List<Section> _sections = new ArrayList<>();

	static {
		parseScript();
	}

	public Section currentSection;
	public Step currentStep;
	public String currentParagraph;

	public WCDeploymentPage( NGContext context ) {
		super( context );
	}

	public List<String> intro() {
		return _intro;
	}

	public List<Section> sections() {
		return _sections;
	}

	private static void parseScript() {
		final List<String> lines;
		try( InputStream in = WCDeploymentPage.class.getResourceAsStream( "/setup-server.sh" ) ) {
			lines = List.of( new String( in.readAllBytes(), StandardCharsets.UTF_8 ).split( "\n", -1 ) );
		}
		catch( final IOException | NullPointerException e ) {
			throw new IllegalStateException( "setup-server.sh missing from the bundle", e );
		}

		Section section = new Section( "Parameters", new ArrayList<>() );
		_sections.add( section );

		final List<String> annotation = new ArrayList<>();
		final List<String> code = new ArrayList<>();
		boolean inIntro = true;
		String heredocTerminator = null;

		for( final String line : lines ) {
			final String stripped = line.strip();

			// Inside a heredoc everything is payload — comments included
			if( heredocTerminator != null ) {
				code.add( line );
				if( stripped.equals( heredocTerminator ) ) {
					heredocTerminator = null;
				}
				continue;
			}

			// Section marker: "### 1 — Title ###..."
			if( stripped.startsWith( "### " ) ) {
				flush( section, annotation, code );
				section = new Section( stripped.replaceAll( "^#+\\s*|\\s*#+$", "" ), new ArrayList<>() );
				_sections.add( section );
				inIntro = false;
				continue;
			}

			// Comment line (but not the shebang): narrative
			if( stripped.startsWith( "#" ) && !stripped.startsWith( "#!" ) ) {
				if( !code.isEmpty() ) {
					flush( section, annotation, code );
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

			// Code. The shebang and set -euo are plumbing, not worth a row.
			if( stripped.startsWith( "#!" ) || stripped.equals( "set -euo pipefail" ) ) {
				continue;
			}
			inIntro = false;
			code.add( line );

			final java.util.regex.Matcher heredoc = java.util.regex.Pattern.compile( "<<\\s*[\"']?(\\w+)[\"']?" ).matcher( line );
			if( heredoc.find() ) {
				heredocTerminator = heredoc.group( 1 );
			}
		}
		flush( section, annotation, code );
		_sections.removeIf( s -> s.steps().isEmpty() );
	}

	private static void flush( final Section section, final List<String> annotation, final List<String> code ) {
		while( !code.isEmpty() && code.getLast().isBlank() ) {
			code.removeLast();
		}
		if( annotation.isEmpty() && code.isEmpty() ) {
			return;
		}
		section.steps().add( new Step( paragraphs( annotation ), String.join( "\n", code ) ) );
		annotation.clear();
		code.clear();
	}

	/** Blank comment lines separate paragraphs. */
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
					current.append( '\n' );
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
