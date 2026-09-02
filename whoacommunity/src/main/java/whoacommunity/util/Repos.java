package whoacommunity.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * For keeping track of github repositories we follow
 */

public class Repos {

	/**
	 * The list of repositories we follow
	 */
	private static List<Repo> _repos;

	/**
	 * @return The list of repositories we follow
	 */
	public static List<Repo> repos() {
		if( _repos == null ) {
			_repos = List.of(
					// Undur repos
					new Repo( Org.undur, "🤸‍♀️", "wonder-slim", "https://github.com/undur/wonder-slim", true ),
					new Repo( Org.undur, "🌿", "parsley", "https://github.com/undur/Parsley", true ),
					new Repo( Org.undur, "🤖", "modulo", "https://github.com/undur/modulo", true ),
					new Repo( Org.undur, "⚙️️", "wonder-slim-deployment", "https://github.com/undur/wonder-slim-deployment", true ),
					new Repo( Org.undur, "🦡", "vermilingua", "https://github.com/undur/vermilingua-maven-plugin", true ),
					new Repo( Org.undur, "📚", "whoacommunity.com", "https://github.com/undur/whoacommunity", true ),
					new Repo( Org.undur, "🔌", "wo-adaptor-jetty", "https://github.com/undur/wo-adaptor-jetty", true ),
					new Repo( Org.undur, "👨‍⚕️", "examiner", "https://github.com/undur/examiner", true ),
					new Repo( Org.undur, "💋", "parslips", "https://github.com/undur/parslips", true ),
					new Repo( Org.undur, "🧠", "parslips-dev-loop-skill", "https://github.com/undur/parslips-dev-loop-skill/", true ),
					new Repo( Org.undur, "🧩", "apiext-format", "https://github.com/undur/apiext-format", true ),

					// Cayenne repos
					new Repo( Org.cayenne, "🌶", "cayenne", "https://github.com/apache/cayenne", true ),

					// WOCommunity repos
					new Repo( Org.wocommunity, "🛠️", "wolips", "https://github.com/wocommunity/wolips", true ),
					new Repo( Org.wocommunity, "🗿", "wonder", "https://github.com/wocommunity/wonder", true ),

					// ng
					new Repo( Org.ng, "🚀", "ng-objects", "https://github.com/ngobjects/ng-objects", true )

			);
		}

		return _repos;
	}

	/**
	 * Repos in our own orgs that don't (yet) warrant a project page
	 */
	private static final Set<String> NOT_PROJECTS = Set.of( "whoacommunity.com", "examiner" );

	/**
	 * @return The repos that get a project page: our own orgs, not the ones we merely follow
	 */
	public static List<Repo> projectRepos() {
		return repos()
				.stream()
				.filter( Repos::isOurs )
				.filter( r -> !NOT_PROJECTS.contains( r.name() ) )
				.toList();
	}

	/**
	 * One-paragraph descriptions for the projects overview. Written here rather than
	 * pulled from GitHub so they say what a project *replaces*, which the repo
	 * descriptions don't.
	 */
	private static final Map<String, String> BLURBS = Map.ofEntries(
			Map.entry( "ng-objects", "A new WO-like web framework, written from scratch: familiar to a WebObjects programmer but without Foundation, EOF or the session lock. Where everything else here is heading." ),
			Map.entry( "wonder-slim", "A slimmed-down fork of Project Wonder with just what a modern WO application needs to run on a current JDK. Seventy-one frameworks became four; Java 1.8 became JDK 25." ),
			Map.entry( "parsley", "The template parser for WebObjects: WOOgnl's inline syntax on ng-objects' parser, with line-and-column errors shown in the page, exception context and a render heat map in development." ),
			Map.entry( "parslips", "WOLips cut down to a single plugin focused on template editing for WO and ng-objects, with refactorings WOLips never had, an element reference, and a dev server that tools and agents can drive." ),
			Map.entry( "apiext-format", "The successor to WO's .api files: typed, directional, documented element bindings, so an editor can check a template against a real contract instead of guessing." ),
			Map.entry( "vermilingua", "A Maven plugin that builds self-contained .woa bundles from scratch rather than wrapping the old Ant tasks. No NEXT_ROOT, no system-wide WO install, and the JVM is switchable at launch." ),
			Map.entry( "wo-adaptor-jetty", "A Jetty-based WOAdaptor for classic WO applications: virtual threads, streaming request bodies and WebSockets, on the same HTTP stack ng-objects uses." ),
			Map.entry( "modulo", "A Jetty reverse proxy that takes the place of mod_WebObjects — and, optionally, a complete front-end server that replaces Apache and certbot too: TLS with native ACME, HTTP/2, WebSockets and hot reload, from one TOML file." ),
			Map.entry( "wonder-slim-deployment", "wotaskd and JavaMonitor, forked from Wonder and cleaned up: Foundation-free, with the wire protocols finally documented and a deploy endpoint that swaps bundles and bounces instances for you." ),
			Map.entry( "parslips-dev-loop-skill", "A Claude Code skill that teaches an agent the edit, refresh, validate loop through the Parslips dev server and the running application's own endpoints." ) );

	/**
	 * @return The project's blurb for the overview page, falling back to nothing (the page shows the GitHub description then)
	 */
	public static String blurbFor( final Repo repo ) {
		return BLURBS.get( repo.name() );
	}

	/**
	 * @return true for repos in our own orgs (undur, ng), as opposed to projects we merely follow
	 */
	public static boolean isOurs( final Repo repo ) {
		return repo.organization() == Org.undur || repo.organization() == Org.ng;
	}

	public static List<Repo> ourRepos() {
		return repos().stream().filter( Repos::isOurs ).toList();
	}

	public static List<Repo> otherRepos() {
		return repos().stream().filter( r -> !isOurs( r ) ).toList();
	}

	/**
	 * @return The project repo with the given name — only repos that have a project page resolve
	 */
	public static Optional<Repo> projectRepoNamed( final String name ) {
		return projectRepos()
				.stream()
				.filter( r -> r.name().equals( name ) )
				.findFirst();
	}

	public enum Org {
		undur,
		wocommunity,
		cayenne,
		ng;

		public List<Repo> repos() {
			return Repos
					.repos()
					.stream()
					.filter( f -> f.organization() == this )
					.toList();
		}
	}

	public record Repo( Org organization, String emoji, String name, String url, boolean includeInGithubFeed ) {

		/**
		 * @return The URL for the repo's atom commit feed
		 */
		public String commitsAtomURL() {
			return url() + "/commits.atom";
		}

		/**
		 * @return The owner part of the GitHub URL (e.g. "undur" in https://github.com/undur/wonder-slim)
		 */
		public String githubOwner() {
			return githubUrlSegment( 0 );
		}

		/**
		 * @return The repo name part of the GitHub URL (e.g. "wonder-slim" in https://github.com/undur/wonder-slim).
		 *         Note: this is the on-GitHub repo name, not our internal display name.
		 */
		public String githubRepoName() {
			return githubUrlSegment( 1 );
		}

		private String githubUrlSegment( int index ) {
			final String prefix = "https://github.com/";
			if( !url.startsWith( prefix ) ) {
				throw new IllegalStateException( "Repo url is not a GitHub URL: " + url );
			}
			final String[] parts = url.substring( prefix.length() ).split( "/" );
			return parts[index];
		}
	}
}