package whoacommunity.util;

import java.util.List;

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

					// Cayenne repos
					new Repo( Org.cayenne, "🌶", "cayenne", "https://github.com/apache/cayenne", true ),

					// WOCommunity repos
					new Repo( Org.wocommunity, "🛠️", "wolips", "https://github.com/wocommunity/wolips", true ),
					new Repo( Org.wocommunity, "📜", "wonder", "https://github.com/wocommunity/wonder", true ),

					// ng
					new Repo( Org.ng, "🚀", "ng-objects", "https://github.com/ngobjects/ng-objects", true )

			);
		}

		return _repos;
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
			return parts[ index ];
		}
	}
}