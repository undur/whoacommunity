package whoacommunity.slack;

import org.apache.cayenne.ObjectContext;

import whoacommunity.app.WCCore;
import whoacommunity.slack.SlackImporter.SlackImportResult;
import whoacommunity.slack.SlackImporter.User;

public class SlackDBImporter {

	public static void main( String[] args ) {
		final ObjectContext oc = WCCore.newContext();

		SlackImportResult run = SlackImporter.defaultImporter().run();

		for( User user : run.users() ) {
			System.out.println( user.id() );
			System.out.println( user.name() );
			System.out.println( user.real_name() );
			System.out.println( user.profile().email() );
			System.out.println( user.profile().image_original() );
			System.out.println( "============" );
		}
	}
}