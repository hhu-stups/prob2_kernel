package de.prob.cosimulation;

public class FMUInvocationException extends RuntimeException {
	private static final long serialVersionUID = 4579969127760670024L;

	public FMUInvocationException(final String message) {
		super(message);
	}

	public FMUInvocationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FMUInvocationException(final Throwable cause) {
		super(cause);
	}
}
