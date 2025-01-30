package whoacommunity.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.ObjectSelect;

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

		if( searchString.length() < 3 ) {
			errorMessage = "Please enter at least three characters in the search string.";
			return null;
		}

		final List<Expression> l = new ArrayList<>();

		l.add( Message.TEXT.containsIgnoreCase( searchString ) );

		if( selectedUser != null ) {
			l.add( Message.USER.eq( selectedUser ) );
		}

		if( selectedChannel != null ) {
			l.add( Message.CHANNEL.eq( selectedChannel ) );
		}

		messages = ObjectSelect
				.query( Message.class )
				.where( ExpressionFactory.and( l ) )
				.prefetch( Message.USER.joint() )
				.prefetch( Message.CHANNEL.joint() )
				.select( WCCore.newContext() );

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