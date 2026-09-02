package whoacommunity.components;

import java.util.List;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.util.Videos;
import whoacommunity.util.Videos.Playlist;
import whoacommunity.util.Videos.Video;

public class WCVideosPage extends WCComponent {

	public Playlist currentPlaylist;
	public Video currentVideo;

	public WCVideosPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "videos";
	}

	@Override
	public String breadcrumbLeaf() {
		return "videos";
	}

	public List<Playlist> playlists() {
		return Videos.playlists();
	}
}
