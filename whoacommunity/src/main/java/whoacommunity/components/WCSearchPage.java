package whoacommunity.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.ObjectSelect;

import is.rebbi.core.util.StringUtilities;
import ng.appserver.NGActionResults;
import ng.appserver.NGComponent;
import ng.appserver.NGContext;
import whoacommunity.app.WCCore;
import whoacommunity.data.Channel;
import whoacommunity.data.Message;
import whoacommunity.data.User;

public class WCSearchPage extends NGComponent {

	public String errorMessage;

	public String searchString;

	public List<Message> messages;
	public Message currentMessage;

	public Channel currentChannel;
	public Channel selectedChannel;

	public User currentUser;
	public User selectedUser;

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
			l.add( Message.TEXT.containsIgnoreCase( searchString ) );
		}

		if( selectedUser != null ) {
			l.add( Message.USER.eq( selectedUser ) );
		}

		if( selectedChannel != null ) {
			l.add( Message.CHANNEL.eq( selectedChannel ) );
		}

		messages = ObjectSelect
				.query( Message.class )
				.where( ExpressionFactory.and( l ) )
				.orderBy( Message.DATE_TIME.asc() )
				.prefetch( Message.USER.joint() )
				.prefetch( Message.CHANNEL.joint() )
				.select( WCCore.newContext() );

		messages = WCSlackArchivePage.filterMessages( messages );

		return null;
	}

	public List<User> users() {
		return ObjectSelect
				.query( User.class )
				.orderBy( User.NAME.ascInsensitive() )
				.select( WCCore.newContext() );
	}

	public List<Channel> channels() {
		return ObjectSelect
				.query( Channel.class )
				.orderBy( Channel.NAME.ascInsensitive() )
				.select( WCCore.newContext() );
	}
}