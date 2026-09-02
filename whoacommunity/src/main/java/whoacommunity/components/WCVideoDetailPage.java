package whoacommunity.components;

import java.util.List;

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

	@Override
	public String pageIdentifier() {
		return "videos";
	}

	@Override
	public List<Crumb> breadcrumbs() {
		return List.of( HOME_CRUMB, new Crumb( "videos", "/videos" ) );
	}

	@Override
	public String breadcrumbLeaf() {
		return video == null ? "" : video.title();
	}

	public Playlist playlist() {
		return Videos.playlistContaining( video ).orElse( null );
	}
}
