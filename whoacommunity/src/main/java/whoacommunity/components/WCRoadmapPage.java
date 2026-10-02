package whoacommunity.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.roadmap.RoadmapFeed;
import whoacommunity.roadmap.RoadmapFeed.Item;

/**
 * The stack roadmap: the board's items by status — what's being done now, what's next, what waits on a
 * decision, and the notebook of ideas — each group arranged by theme, since themes cross repositories.
 */
public class WCRoadmapPage extends WCComponent {

	/**
	 * A status column of the board as the page shows it
	 */
	public record Group( String status, String title, String subtitle, List<ThemeBlock> themes, int count ) {}

	/**
	 * The items of one theme within a group
	 */
	public record ThemeBlock( String theme, List<Item> items ) {}

	public Group currentGroup;
	public ThemeBlock currentTheme;
	public Item currentItem;

	public WCRoadmapPage( NGContext context ) {
		super( context );
	}

	@Override
	public String pageIdentifier() {
		return "roadmap";
	}

	@Override
	public String pageTitle() {
		return "Stack roadmap";
	}

	@Override
	public String pageDescription() {
		return "Where the WebObjects and ng-objects stack is going: the open issues across every repository, organized by theme into what's being done now, what's next, what waits on a decision, and the notebook of ideas.";
	}

	@Override
	public String breadcrumbLeaf() {
		return "roadmap";
	}

	public boolean isLoaded() {
		return RoadmapFeed.shared.isLoaded();
	}

	public String projectURL() {
		return RoadmapFeed.PROJECT_URL;
	}

	public int itemCount() {
		return RoadmapFeed.shared.items().size();
	}

	public List<Group> groups() {
		return List.of(
				group( "Now", "Now", "what's being worked on now" ),
				group( "Next", "Next", "lined up after that" ),
				group( "Decide", "Waiting on a decision", "can't move until a call is made" ),
				group( "Someday", "The notebook", "ideas and design notes, kept on file" ) );
	}

	private static Group group( final String status, final String title, final String subtitle ) {
		final Map<String, List<Item>> byTheme = new LinkedHashMap<>();

		for( final Item item : RoadmapFeed.shared.withStatus( status ) ) {
			byTheme.computeIfAbsent( item.theme() == null ? "other" : item.theme(), k -> new ArrayList<>() ).add( item );
		}

		final List<ThemeBlock> themes = new ArrayList<>();
		int count = 0;

		for( final Map.Entry<String, List<Item>> e : byTheme.entrySet() ) {
			themes.add( new ThemeBlock( e.getKey(), e.getValue() ) );
			count += e.getValue().size();
		}

		themes.sort( ( a, b ) -> Integer.compare( b.items().size(), a.items().size() ) );
		return new Group( status, title, subtitle, themes, count );
	}

	public boolean currentGroupIsEmpty() {
		return currentGroup.count() == 0;
	}

	public String currentGroupClass() {
		return "roadmap-group roadmap-" + currentGroup.status().toLowerCase();
	}

	public String currentItemKindClass() {
		return "roadmap-kind roadmap-kind-" + ( currentItem.kind() == null ? "none" : currentItem.kind().replace( ' ', '-' ) );
	}

	public boolean currentItemHasKind() {
		return currentItem.kind() != null;
	}

	public boolean currentItemHasEffort() {
		return currentItem.effort() != null;
	}

	public String currentItemRef() {
		return currentItem.repoName() + " #" + currentItem.number();
	}
}
