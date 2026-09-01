package whoacommunity.components;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import whoacommunity.app.WCComponent;
import whoacommunity.data.Article;
import whoacommunity.data.Comment;

public class WCArticleDetailPage extends WCComponent {

	public Article selectedObject;
	public Comment currentComment;

	// Comment form fields
	public String newName;
	public String newContent;
	public String newAnswer;

	/**
	 * Honeypot: a text field hidden from humans by CSS. Real people never
	 * fill it, form-filling bots do — anything in here is silently dropped.
	 */
	public String honeypot;

	public String errorMessage;
	public boolean justPosted;

	/**
	 * When the form was last shown. Bots that fetch-then-post within a
	 * couple of seconds get rejected; humans take longer to type.
	 */
	private Instant _formShownAt = Instant.now();

	private static final Duration MIN_TIME_TO_POST = Duration.ofSeconds( 3 );
	private static final int MAX_NAME_LENGTH = 100;
	private static final int MAX_CONTENT_LENGTH = 10_000;

	/**
	 * The "are you one of us" question. Trivial for the audience, opaque to
	 * generic spam bots. Answer matching is lenient: case-insensitive, and
	 * anything that isn't a letter is ignored, so "Web Objects" passes.
	 */
	public static final String QUESTION = "What does the \"WO\" in WOCommunity stand for?";
	private static final String ANSWER = "webobjects";

	public WCArticleDetailPage( NGContext context ) {
		super( context );
	}

	public List<Comment> comments() {
		return selectedObject.sortedComments();
	}

	public String question() {
		return QUESTION;
	}

	public NGActionResults postComment() {
		errorMessage = null;
		justPosted = false;

		// Bots: pretend success, store nothing, give them no signal to adapt to.
		if( honeypot != null && !honeypot.isBlank() ) {
			clearForm();
			justPosted = true;
			return null;
		}

		if( Duration.between( _formShownAt, Instant.now() ).compareTo( MIN_TIME_TO_POST ) < 0 ) {
			errorMessage = "That was quick! Please try posting again.";
			_formShownAt = Instant.now();
			return null;
		}

		final String name = newName == null ? "" : newName.strip();
		final String content = newContent == null ? "" : newContent.strip();

		if( name.isEmpty() || content.isEmpty() ) {
			errorMessage = "Both a name and a comment are needed.";
			return null;
		}

		if( name.length() > MAX_NAME_LENGTH || content.length() > MAX_CONTENT_LENGTH ) {
			errorMessage = "That's a bit long — please keep the name under %s and the comment under %s characters.".formatted( MAX_NAME_LENGTH, MAX_CONTENT_LENGTH );
			return null;
		}

		if( !isCorrectAnswer( newAnswer ) ) {
			errorMessage = "Hmm, that's not the WO we know. Try again?";
			return null;
		}

		final Comment comment = selectedObject.getObjectContext().newObject( Comment.class );
		comment.setName( name );
		comment.setContent( content );
		comment.setDateTime( LocalDateTime.now() );
		comment.setArticle( selectedObject );
		selectedObject.getObjectContext().commitChanges();

		clearForm();
		justPosted = true;
		return null;
	}

	/**
	 * Admin-only (guarded by $isLocal in the template)
	 */
	public NGActionResults deleteComment() {
		if( !isLocal() ) {
			return null;
		}

		selectedObject.getObjectContext().deleteObject( currentComment );
		selectedObject.getObjectContext().commitChanges();
		return null;
	}

	private static boolean isCorrectAnswer( final String answer ) {
		if( answer == null ) {
			return false;
		}

		return ANSWER.equals( answer.toLowerCase().replaceAll( "[^a-z]", "" ) );
	}

	private void clearForm() {
		newName = null;
		newContent = null;
		newAnswer = null;
		honeypot = null;
		_formShownAt = Instant.now();
	}
}
