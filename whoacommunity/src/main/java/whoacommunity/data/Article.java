package whoacommunity.data;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import jambalaya.interfaces.DateTimeStamped;
import jambalaya.interfaces.UUIDStamped;
import whoacommunity.data.auto._Article;
import whoacommunity.util.Markdown;

public class Article extends _Article implements DateTimeStamped, UUIDStamped {

	/**
	 * FIXME: Nicer enum names, following naming conventions, with the stored DB value as a code field 	// Hugi 2025-07-06
	 */
	public enum ArticleFormat {
		html,
		markdown;

		public String code() {
			return toString();
		}
	}

	private static final int TEASER_LENGTH = 220;

	@Override
	protected void onPostAdd() {
		setPublished( false );
		setFormatCode( ArticleFormat.markdown.toString() );
	}

	/**
	 * @return the article's content formatted as HTML
	 */
	public String contentAsHTML() {
		return switch( format() ) {
			case html -> content();
			case markdown -> Markdown.render( content() );
		};
	}

	/**
	 * @return The first couple of sentences of the article as plain text, for teaser rows
	 */
	public String teaser() {
		final String text = Markdown.plainText( contentAsHTML() );

		if( text.length() <= TEASER_LENGTH ) {
			return text;
		}

		int cut = text.lastIndexOf( ' ', TEASER_LENGTH );

		if( cut < TEASER_LENGTH / 2 ) {
			cut = TEASER_LENGTH;
		}

		return text.substring( 0, cut ).strip() + "…";
	}

	/**
	 * FIXME: Articles don't carry an author yet; there's exactly one. Make it a field when that changes // Hugi 2026-09-02
	 */
	public String authorName() {
		return "Hugi Thordarson";
	}

	public String formattedDate() {
		return date().format( DateTimeFormatter.ofPattern( "MMMM d, YYYY" ) );
	}

	public ArticleFormat format() {
		return ArticleFormat.valueOf( formatCode() );
	}

	public void setFormat( ArticleFormat value ) {
		setFormatCode( value.code() );
	}

	public String shortDateFormatted() {
		return date().format( DateTimeFormatter.ofPattern( "MMM d" ) );
	}

	/**
	 * @return Comments in the order they were posted
	 */
	public List<Comment> sortedComments() {
		return comments()
				.stream()
				.sorted( Comparator.comparing( Comment::dateTime ) )
				.toList();
	}

	public int commentCount() {
		return comments().size();
	}

	public boolean hasComments() {
		return commentCount() > 0;
	}

	/**
	 * @return "1 comment" / "5 comments" for display
	 */
	public String commentCountString() {
		final int count = commentCount();
		return count == 1 ? "1 comment" : count + " comments";
	}
}
