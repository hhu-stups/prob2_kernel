package de.prob.animator.domainobjects;

import java.util.List;
import java.util.stream.Collectors;

public class WDError extends EvaluationErrorResult {
	public static final String MESSAGE = "NOT-WELL-DEFINED";

	public WDError(final String result, final List<ErrorItem> errors) {
		super(result, errors);
	}

	public WDError(final List<String> errors) {
		this(MESSAGE, errors.stream()
			.map(ErrorItem::fromErrorMessage)
			.collect(Collectors.toList()));
	}
}
