package de.prob.animator.command;

public class UnknownLtlResult extends RuntimeException {
	private static final long serialVersionUID = -867476809423698083L;

	public UnknownLtlResult(final String message) {
		super(message);
	}

	public UnknownLtlResult(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnknownLtlResult(final Throwable cause) {
		super(cause);
	}
}
