package de.prob.ui.api;

public class IllegalFormulaException extends Exception {
	private static final long serialVersionUID = 592717348232703388L;

	public IllegalFormulaException(final String message) {
		super(message);
	}
	
	public IllegalFormulaException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public IllegalFormulaException(final Throwable cause) {
		super(cause);
	}
}
