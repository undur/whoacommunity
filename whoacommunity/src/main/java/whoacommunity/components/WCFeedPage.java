package whoacommunity.components;

import java.util.List;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;

public class WCFeedPage extends WCComponent {

	public Item currentItem;

	public WCFeedPage( NGContext context ) {
		super( context );
	}

	public List<Item> items() {
		final List<String> urls = List.of(
				"https://github.com/undur/wonder-slim/commits.atom",
				"https://github.com/undur/parsley/commits.atom",
				"https://github.com/undur/modulo/commits.atom",
				"https://github.com/undur/wonder-slim-deployment/commits.atom",
				"https://github.com/undur/vermilingua-maven-plugin/commits.atom",
				//				"https://github.com/undur/whoacommunity/commits.atom",
				"https://github.com/undur/examiner/commits.atom" );

		return new RssReader()
				.read( urls )
				.sorted()
				.toList();
	}
}