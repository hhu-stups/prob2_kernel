package de.prob.model.eventb;

public class ModelGenerationException extends Exception {
	private static final long serialVersionUID = 7416830100160162264L;

	public ModelGenerationException(final String message) {
		super(message);
	}

	public ModelGenerationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ModelGenerationException(final Throwable cause) {
		super(cause);
	}
}
