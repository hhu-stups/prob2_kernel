package de.prob.visualization;

import de.prob.statespace.AnimationSelector;

/**
 * This {@link Exception} is thrown when a visualization is opened but there is no animation loaded in the {@link AnimationSelector}.
 * 
 * @author joy
 */
public class AnimationNotLoadedException extends RuntimeException {
	private static final long serialVersionUID = 2066408268095355170L;

	public AnimationNotLoadedException(final String message) {
		super(message);
	}

	public AnimationNotLoadedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AnimationNotLoadedException(final Throwable cause) {
		super(cause);
	}
}
