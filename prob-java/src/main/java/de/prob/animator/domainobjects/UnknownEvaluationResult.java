package de.prob.animator.domainobjects;

import java.util.List;
import java.util.stream.Collectors;

public class UnknownEvaluationResult extends EvaluationErrorResult {
	public static final String MESSAGE = "UNKNOWN";

	public UnknownEvaluationResult(final String result, final List<ErrorItem> errors) {
		super(result, errors);
	}

	public UnknownEvaluationResult(final List<String> errors) {
		this(MESSAGE, errors.stream()
			.map(ErrorItem::fromErrorMessage)
			.collect(Collectors.toList()));
	}
}
