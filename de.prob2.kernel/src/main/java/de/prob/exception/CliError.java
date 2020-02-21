package de.prob.exception;

public class CliError extends RuntimeException {
	private static final long serialVersionUID = -2546117910718258435L;

	public CliError(final String message) {
		super(message);
	}

	public CliError(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CliError(final Throwable cause) {
		super(cause);
	}
}
