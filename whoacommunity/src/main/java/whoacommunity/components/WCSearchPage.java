package whoacommunity.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.ObjectSelect;

import is.rebbi.core.util.StringUtilities;
import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import whoacommunity.app.WCCore;
import whoacommunity.data.SlackChannel;
import whoacommunity.data.SlackMessage;
import whoacommunity.data.SlackUser;

public class WCSearchPage extends NGComponent {

	public String errorMessage;

	public String searchString;

	public List<SlackMessage> messages;
	public SlackMessage currentMessage;

	public SlackChannel currentChannel;
	public SlackChannel selectedChannel;

	public SlackUser currentUser;
	public SlackUser selectedUser;

	public WCSearchPage( NGContext context ) {
		super( context );
	}

	public NGActionResults search() {
		errorMessage = null;

		// If you're searching by string only, you don't get to search for really short string
		if( (searchString == null || searchString.length() < 3) && selectedChannel == null && selectedUser == null ) {
			errorMessage = "Please enter at least three characters in the search string if you're not searching by any other conditions.";
			return null;
		}

		final List<Expression> l = new ArrayList<>();

		if( StringUtilities.hasValue( searchString ) ) {
			l.add( SlackMessage.TEXT.containsIgnoreCase( searchString ) );
		}

		if( selectedUser != null ) {
			l.add( SlackMessage.USER.eq( selectedUser ) );
		}

		if( selectedChannel != null ) {
			l.add( SlackMessage.CHANNEL.eq( selectedChannel ) );
		}

		messages = ObjectSelect
				.query( SlackMessage.class )
				.where( ExpressionFactory.and( l ) )
				.orderBy( SlackMessage.DATE_TIME.asc() )
				.prefetch( SlackMessage.USER.joint() )
				.prefetch( SlackMessage.CHANNEL.joint() )
				.select( WCCore.newContext() );

		messages = WCSlackArchivePage.filterMessages( messages );

		return null;
	}

	public List<SlackUser> users() {
		return ObjectSelect
				.query( SlackUser.class )
				.orderBy( SlackUser.NAME.ascInsensitive() )
				.select( WCCore.newContext() );
	}

	public List<SlackChannel> channels() {
		return ObjectSelect
				.query( SlackChannel.class )
				.orderBy( SlackChannel.NAME.ascInsensitive() )
				.select( WCCore.newContext() );
	}
}