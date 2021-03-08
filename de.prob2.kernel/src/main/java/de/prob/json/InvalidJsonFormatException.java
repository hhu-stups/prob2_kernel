package de.prob.json;

/**
 * Thrown by {@link JacksonManager} and related code
 * when attempting to load JSON data with an unexpected file type or unsupported format version.
 */
public final class InvalidJsonFormatException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public InvalidJsonFormatException(final String message) {
		this(message, null);
	}
	
	public InvalidJsonFormatException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public InvalidJsonFormatException(final Throwable cause) {
		super(cause);
	}
}
