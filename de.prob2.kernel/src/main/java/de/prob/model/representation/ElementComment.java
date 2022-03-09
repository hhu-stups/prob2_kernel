package de.prob.model.representation;

import java.util.stream.Collectors;

/**
 * Allows adding additional information to any element within the model domain
 * @author joy
 *
 */
public class ElementComment extends AbstractElement {
	private String comment;

	public ElementComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	/**
	 * Helper method for getting an element's comment(s) as a simple string.
	 * If the element has multiple {@link ElementComment}s attached (should not usually happen),
	 * they are joined with newlines.
	 * 
	 * @param element the element from which to get the comment text
	 * @return the comment text, or {@code null} if {@code element} has no comments attached
	 */
	public static String getCommentTextFromElement(final AbstractElement element) {
		final ModelElementList<ElementComment> comments = element.getChildrenOfType(ElementComment.class);
		if (comments == null) {
			return null;
		} else {
			return comments.stream()
				.map(ElementComment::getComment)
				.collect(Collectors.joining("\n"));
		}
	}
}
