package whoacommunity.components;

import java.util.List;

import org.apache.cayenne.query.ObjectSelect;

import ng.appserver.NGComponent;
import ng.appserver.NGContext;
import whoacommunity.app.WCCore;
import whoacommunity.data.Channel;

public class WCMain extends NGComponent {

	public Channel currentChannel;

	public WCMain( NGContext context ) {
		super( context );
	}

	public List<Channel> channels() {
		return ObjectSelect
				.query( Channel.class )
				.orderBy( Channel.NAME.ascInsensitive() )
				.select( WCCore.newContext() );
	}

	public String currentChannelURL() {
		return "/channel/" + currentChannel.name();
	}
}