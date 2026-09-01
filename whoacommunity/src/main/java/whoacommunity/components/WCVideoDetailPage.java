package whoacommunity.components;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.Videos;
import whoacommunity.util.Videos.Playlist;
import whoacommunity.util.Videos.Video;

public class WCVideoDetailPage extends WCComponent {

	public Video video;

	public WCVideoDetailPage( NGContext context ) {
		super( context );
	}

	public Playlist playlist() {
		return Videos.playlistContaining( video ).orElse( null );
	}
}
