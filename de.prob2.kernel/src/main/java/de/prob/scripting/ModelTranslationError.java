package de.prob.scripting;

import de.prob.exception.ProBError;

/**
 * @deprecated No longer used - replaced by {@link ProBError}.
 */
@Deprecated
public class ModelTranslationError extends Exception {
	private static final long serialVersionUID = 3192220775081118164L;

	public ModelTranslationError(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public ModelTranslationError(final Throwable cause) {
		super(cause);
	}
}
