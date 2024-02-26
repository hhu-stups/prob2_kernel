package de.prob.exception;

/**
 * Represents an unhandled Prolog exception (from throw/1 in Prolog).
 */
public final class PrologException extends ProBError {
	private static final long serialVersionUID = 1L;
	
	private final String exceptionTermString;
	
	public PrologException(final String exceptionTermString) {
		this(exceptionTermString, null);
	}
	
	public PrologException(final String exceptionTermString, final Throwable cause) {
		super("Unhandled exception thrown from Prolog: " + exceptionTermString, cause);
		
		this.exceptionTermString = exceptionTermString;
	}
	
	/**
	 * Get the thrown Prolog term as an unparsed string.
	 * 
	 * @return the thrown Prolog term as an unparsed string
	 */
	public String getExceptionTermString() {
		return this.exceptionTermString;
	}
}
