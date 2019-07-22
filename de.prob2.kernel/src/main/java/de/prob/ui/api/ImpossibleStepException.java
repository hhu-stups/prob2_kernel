package de.prob.ui.api;

public class ImpossibleStepException extends Exception {
	private static final long serialVersionUID = 4510067758983980232L;

	public ImpossibleStepException(final String message) {
		super(message);
	}

	public ImpossibleStepException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ImpossibleStepException(final Throwable cause) {
		super(cause);
	}
}
