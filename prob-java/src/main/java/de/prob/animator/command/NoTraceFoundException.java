package de.prob.animator.command;

public class NoTraceFoundException extends RuntimeException {
	private static final long serialVersionUID = 2607058283175307354L;

	public NoTraceFoundException(final String message) {
		super(message);
	}

	public NoTraceFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoTraceFoundException(final Throwable cause) {
		super(cause);
	}
}
