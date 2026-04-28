package whoacommunity.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Minimal GitHub GraphQL v4 client. One method, run a query and get the
 * "data" object back.
 */
public class GithubGraphQLClient {

	private static final URI ENDPOINT = URI.create( "https://api.github.com/graphql" );

	private final String _token;
	private final HttpClient _http;
	private final Gson _gson;

	public GithubGraphQLClient( final String token ) {
		_token = token;
		_http = HttpClient.newBuilder().connectTimeout( Duration.ofSeconds( 10 ) ).build();
		_gson = new Gson();
	}

	/**
	 * Run a GraphQL query. Returns the value of the "data" field on success.
	 * Throws on transport errors or any GraphQL error returned by the API.
	 */
	public JsonObject query( final String query ) throws IOException, InterruptedException {
		final String body = _gson.toJson( Map.of( "query", query ) );

		final HttpRequest request = HttpRequest.newBuilder( ENDPOINT )
				.header( "Authorization", "Bearer " + _token )
				.header( "Content-Type", "application/json" )
				.header( "Accept", "application/json" )
				.header( "User-Agent", "whoacommunity.com" )
				.timeout( Duration.ofSeconds( 30 ) )
				.POST( HttpRequest.BodyPublishers.ofString( body ) )
				.build();

		final HttpResponse<String> response = _http.send( request, BodyHandlers.ofString() );

		if( response.statusCode() / 100 != 2 ) {
			throw new IOException( "GitHub GraphQL HTTP " + response.statusCode() + ": " + response.body() );
		}

		final JsonObject parsed = _gson.fromJson( response.body(), JsonObject.class );

		if( parsed.has( "errors" ) ) {
			throw new IOException( "GitHub GraphQL errors: " + parsed.get( "errors" ) );
		}

		final JsonElement data = parsed.get( "data" );

		if( data == null || !data.isJsonObject() ) {
			throw new IOException( "GitHub GraphQL response missing 'data' field: " + response.body() );
		}

		return data.getAsJsonObject();
	}
}
