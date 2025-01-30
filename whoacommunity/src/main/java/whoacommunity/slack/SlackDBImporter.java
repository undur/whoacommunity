package whoacommunity.slack;

import org.apache.cayenne.ObjectContext;

import whoacommunity.app.WCCore;
import whoacommunity.slack.SlackImporter.SlackImportResult;

public class SlackDBImporter {

	public static void main( String[] args ) {
		final ObjectContext oc = WCCore.newContext();

		SlackImportResult run = SlackImporter.defaultImporter().run();

		//		for( SlackUser slackUser : run.users() ) {
		//			final User user = oc.newObject( User.class );
		//			user.setSlackID( slackUser.id() );
		//			user.setSlackUsername( slackUser.name() );
		//			user.setName( slackUser.real_name() );
		//			user.setEmailAddress( slackUser.profile().email() );
		//			user.setSlackProfileImageUrl( slackUser.profile().image_original() );
		//		}
		//
		//		oc.commitChanges();
	}
}