package de.prob.cosimulation;

public class CantInstantiateFMUException extends RuntimeException {
	private static final long serialVersionUID = -7746684176620164353L;

	public CantInstantiateFMUException(final String message) {
		super(message);
	}

	public CantInstantiateFMUException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CantInstantiateFMUException(final Throwable cause) {
		super(cause);
	}
}
