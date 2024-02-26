package de.prob.animator;

import java.util.List;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.exception.ProBError;

public final class CommandInterruptedException extends ProBError {
	private static final long serialVersionUID = 1L;
	
	public CommandInterruptedException(final String message, final List<ErrorItem> errors) {
		this(message, errors, null);
	}
	
	public CommandInterruptedException(final String message, final List<ErrorItem> errors, final Throwable cause) {
		super(message, errors, cause);
	}
}
