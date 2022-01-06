package de.prob.json;

/**
 * Thrown by {@link JacksonManager} and related code
 * if JSON data in an old format could not be converted to the current format. 
 */
public final class JsonConversionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public JsonConversionException(final String message) {
		this(message, null);
	}
	
	public JsonConversionException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public JsonConversionException(final Throwable cause) {
		super(cause);
	}
}
