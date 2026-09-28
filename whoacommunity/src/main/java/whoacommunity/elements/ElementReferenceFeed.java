package whoacommunity.elements;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import whoacommunity.util.CachedFeed;

/**
 * The element reference, fetched as JSON from the AjaxPlayground (wonder-slim's playground application),
 * which renders it from the libraries' {@code .apiext} files. The playground publishes an index of sources at
 * {@code /element-reference/sources.json} and each source's elements at the path the index names.
 *
 * Cached and refreshed in the background like the GitHub data; a failed fetch keeps the previous value.
 * The base URL is {@code wc.elementReferenceURL}, defaulting to the public playground.
 */
public class ElementReferenceFeed {

	public static final String DEFAULT_BASE_URL = "https://www.skoffin.com";

	public static final ElementReferenceFeed shared = new ElementReferenceFeed( Duration.ofHours( 1 ) );

	private static final Gson GSON = new Gson();
	private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout( Duration.ofSeconds( 10 ) ).followRedirects( HttpClient.Redirect.NORMAL ).build();

	private final CachedFeed<List<Source>> _feed;

	public ElementReferenceFeed( final Duration cacheDuration ) {
		_feed = new CachedFeed<>( cacheDuration, ElementReferenceFeed::fetch, List.of() );
	}

	/**
	 * A library the reference documents. {@code slug} is the last path element of the playground's page path,
	 * which is what our own URLs use: /element-reference/{slug}. The AjaxSlim source lives at the playground's
	 * reference root, so its slug is derived from its id instead.
	 */
	public record Source( String id, String path, String json, String title, String leadHtml, List<Element> elements ) {

		public String slug() {
			final String last = path.substring( path.lastIndexOf( '/' ) + 1 );
			return "element-reference".equals( last ) ? id.toLowerCase() : last;
		}

		public Source withElements( final List<Element> elements ) {
			return new Source( id, path, json, title, localizeLinks( leadHtml ), elements );
		}
	}

	public record Element(
			String className,
			String docHtml,
			boolean deprecated,
			String deprecatedHtml,
			boolean expectsContent,
			boolean passthrough,
			boolean forbidsUnknownAttributes,
			List<String> tags,
			List<Binding> bindings,
			List<Constraint> constraints ) {

		public boolean hasTags() {
			return tags != null && !tags.isEmpty();
		}

		public boolean hasBindings() {
			return bindings != null && !bindings.isEmpty();
		}

		public boolean hasConstraints() {
			return constraints != null && !constraints.isEmpty();
		}
	}

	public record Binding(
			String name,
			boolean required,
			String docHtml,
			String defaultValue,
			boolean deprecated,
			String deprecatedHtml,
			String direction,
			String pullType,
			String pushType ) {

		public boolean hasDefault() {
			return defaultValue != null;
		}

		/**
		 * @return true when the binding reads one type and writes another, so the type column shows both
		 */
		public boolean typeSplit() {
			return pullType != null && pushType != null && !pullType.equals( pushType );
		}

		/**
		 * @return The single type to show when the binding isn't split; null when the file declares none
		 */
		public String displayType() {
			return pullType != null ? pullType : pushType;
		}

		public boolean hasDirection() {
			return direction != null;
		}

		public String directionArrow() {
			return direction == null ? "" : switch( direction ) {
				case "both" -> "↕";
				case "push" -> "↑";
				default -> "↓";
			};
		}

		public String directionTitle() {
			return direction == null ? "" : switch( direction ) {
				case "both" -> "Two-way: read and written back";
				case "push" -> "Pushed: written back to the component";
				default -> "Pulled: read from the component";
			};
		}

		public String directionClass() {
			return "dir dir-" + ( direction == null ? "none" : direction );
		}
	}

	public record Constraint( String kind, String message ) {}

	/** The playground's index shape */
	private record Index( List<Source> sources ) {}

	/** The playground's per-source shape */
	private record SourceDocument( String id, String title, String leadHtml, List<Element> elements ) {}

	public List<Source> sources() {
		return _feed.value();
	}

	public Source sourceForSlug( final String slug ) {
		return sources().stream().filter( s -> s.slug().equals( slug ) ).findFirst().orElse( null );
	}

	public boolean isLoaded() {
		return !sources().isEmpty();
	}

	private static final java.util.regex.Pattern ROOT_HREF = java.util.regex.Pattern.compile( "href=\"(/[^\"]*)\"" );

	/**
	 * The playground's text links to its own pages by root-relative path. Its element-reference pages exist here
	 * too, so those stay local (mapped to our slugs); every other root-relative link goes to the playground.
	 */
	static String localizeLinks( final String html ) {
		if( html == null ) {
			return null;
		}

		final java.util.regex.Matcher m = ROOT_HREF.matcher( html );
		final StringBuilder out = new StringBuilder();

		while( m.find() ) {
			final String path = m.group( 1 );
			final String target;

			if( path.equals( "/element-reference" ) ) {
				target = "/element-reference/ajaxslim";
			}
			else if( path.startsWith( "/element-reference/" ) ) {
				target = path;
			}
			else {
				target = baseURL() + path;
			}

			m.appendReplacement( out, java.util.regex.Matcher.quoteReplacement( "href=\"" + target + "\"" ) );
		}

		m.appendTail( out );
		return out.toString();
	}

	public static String baseURL() {
		final String configured = System.getProperty( "wc.elementReferenceURL" );
		final String base = configured == null || configured.isBlank() ? DEFAULT_BASE_URL : configured;
		return base.endsWith( "/" ) ? base.substring( 0, base.length() - 1 ) : base;
	}

	private static List<Source> fetch() {
		try {
			final Index index = GSON.fromJson( get( baseURL() + "/element-reference/sources.json" ), Index.class );
			final List<Source> out = new ArrayList<>();

			for( final Source source : index.sources() ) {
				final SourceDocument document = GSON.fromJson( get( baseURL() + source.json() ), SourceDocument.class );
				out.add( source.withElements( document.elements() == null ? List.of() : document.elements() ) );
			}

			return List.copyOf( out );
		}
		catch( final Exception e ) {
			System.err.println( "ElementReferenceFeed: refresh failed, keeping the previous value: " + e );
			throw new RuntimeException( e );
		}
	}

	private static String get( final String url ) throws Exception {
		final HttpRequest request = HttpRequest.newBuilder( URI.create( url ) ).timeout( Duration.ofSeconds( 30 ) ).header( "accept", "application/json" ).GET().build();
		final HttpResponse<String> response = CLIENT.send( request, HttpResponse.BodyHandlers.ofString() );

		if( response.statusCode() != 200 ) {
			throw new IllegalStateException( "HTTP " + response.statusCode() + " from " + url );
		}

		return response.body();
	}
}
