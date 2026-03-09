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
					new Repo( Org.undur, "🤸‍♀️", "wonder-slim", "https://github.com/undur/wonder-slim" ),
					new Repo( Org.undur, "🌿", "parsley", "https://github.com/undur/Parsley" ),
					new Repo( Org.undur, "🤖", "modulo", "https://github.com/undur/modulo" ),
					new Repo( Org.undur, "⚙️️", "wonder-slim-deployment", "https://github.com/undur/wonder-slim-deployment" ),
					new Repo( Org.undur, "🦡", "vermilingua", "https://github.com/undur/vermilingua-maven-plugin" ),
					new Repo( Org.undur, "📚", "whoacommunity.com", "https://github.com/undur/whoacommunity" ),
					new Repo( Org.undur, "🔌", "wo-adaptor-jetty", "https://github.com/undur/wo-adaptor-jetty" ),
					new Repo( Org.undur, "👨‍⚕️", "examiner", "https://github.com/undur/examiner" ),
					new Repo( Org.undur, "💋", "parslips", "https://github.com/undur/parslips" ),

					// Cayenne repos
					new Repo( Org.cayenne, "🌶", "cayenne", "https://github.com/apache/cayenne" ),

					// WOCommunity repos
					new Repo( Org.wocommunity, "🛠️", "wolips", "https://github.com/wocommunity/wolips" ),
					new Repo( Org.wocommunity, "📜", "wonder", "https://github.com/wocommunity/wonder" ),

					// ng
					new Repo( Org.ng, "🚀", "ng-objects", "https://github.com/ngobjects/ng-objects" )

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

	public record Repo( Org organization, String emoji, String name, String url ) {

		/**
		 * @return The URL for the repo's atom commit feed
		 */
		public String commitsAtomURL() {
			return url() + "/commits.atom";
		}
	}
}