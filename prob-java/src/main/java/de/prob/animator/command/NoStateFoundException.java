package de.prob.animator.command;

public class NoStateFoundException extends RuntimeException {
	private static final long serialVersionUID = -7320372458222099380L;

	public NoStateFoundException(final String message) {
		super(message);
	}

	public NoStateFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NoStateFoundException(final Throwable cause) {
		super(cause);
	}
}
