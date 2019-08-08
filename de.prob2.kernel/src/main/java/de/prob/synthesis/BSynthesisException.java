package de.prob.synthesis;

public class BSynthesisException extends Exception {
	private static final long serialVersionUID = 1L;

	public BSynthesisException(final String message) {
		super(message);
	}

	public BSynthesisException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public BSynthesisException(final Throwable cause) {
		super(cause);
	}

	public String getMsg() {
		return this.getMessage();
	}
}
