package de.prob.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.animator.domainobjects.ErrorItem;

public class ProBError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final String originalMessage;
	private final List<ErrorItem> errorItems;

	private static String formatMessageAndErrors(final String message, final List<ErrorItem> errors) {
		final StringBuilder out = new StringBuilder();

		if (message != null && !message.isEmpty()) {
			out.append(message);
			if (!errors.isEmpty()) {
				out.append('\n');
			}
		}

		if (!errors.isEmpty()) {
			out.append("ProB returned error messages:");
			for (final ErrorItem err : errors) {
				out.append('\n');
				out.append(err);
			}
		}

		return out.toString();
	}

	public ProBError(final String message, final List<ErrorItem> errors, final Throwable cause) {
		// errors == null is deprecated, but still supported for compatibility (just in case).
		// New code should pass an empty list instead.
		super(formatMessageAndErrors(message, errors == null ? Collections.emptyList() : errors), cause);
		this.originalMessage = message;
		this.errorItems = errors == null ? Collections.emptyList() : new ArrayList<>(errors);
	}

	public ProBError(final String message, final List<ErrorItem> errors) {
		this(message, errors, null);
	}

	public ProBError(final List<ErrorItem> errors) {
		this(null, errors, null);
	}

	public ProBError(final String message, final Throwable cause) {
		this(message, Collections.emptyList(), cause);
	}

	public ProBError(final String message) {
		this(message, Collections.emptyList(), null);
	}

	public ProBError(final Throwable cause) {
		this(cause.getMessage(), Collections.emptyList(), cause);
	}

	public ProBError(BCompoundException e) {
		this(null, convertParserExceptionToErrorItems(e), e);
	}

	private static List<ErrorItem> convertParserExceptionToErrorItems(BCompoundException e) {
		return e.getBExceptions().stream()
			.map(ErrorItem::fromParserException)
			.collect(Collectors.toList());
	}

	public String getOriginalMessage() {
		return this.originalMessage;
	}

	public List<ErrorItem> getErrors() {
		return Collections.unmodifiableList(this.errorItems);
	}
}
