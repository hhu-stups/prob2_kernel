package de.prob.exception;

import java.util.Objects;

import de.prob.prolog.term.PrologTerm;

/**
 * Represents an unhandled Prolog exception (from throw/1 in Prolog).
 */
@SuppressWarnings("serial")
public final class PrologException extends ProBError {
	private final PrologTerm exceptionTerm;
	
	public PrologException(PrologTerm exceptionTerm) {
		this(exceptionTerm, null);
	}
	
	public PrologException(PrologTerm exceptionTerm, Throwable cause) {
		super("Unhandled exception thrown from Prolog: " + exceptionTerm, cause);
		
		this.exceptionTerm = Objects.requireNonNull(exceptionTerm, "exceptionTerm");
	}
	
	public PrologTerm getExceptionTerm() {
		return this.exceptionTerm;
	}
	
	/**
	 * Get the thrown Prolog term as an unparsed string.
	 * 
	 * @return the thrown Prolog term as an unparsed string
	 */
	public String getExceptionTermString() {
		return this.exceptionTerm.toString();
	}
}
