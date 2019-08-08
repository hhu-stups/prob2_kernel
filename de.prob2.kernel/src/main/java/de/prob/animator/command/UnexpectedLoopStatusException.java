package de.prob.animator.command;

public class UnexpectedLoopStatusException extends RuntimeException {
	private static final long serialVersionUID = -431177400835031162L;

	public UnexpectedLoopStatusException(final String message) {
		super(message);
	}

	public UnexpectedLoopStatusException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnexpectedLoopStatusException(final Throwable cause) {
		super(cause);
	}
}
