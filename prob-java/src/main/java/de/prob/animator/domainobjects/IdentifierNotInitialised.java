package de.prob.animator.domainobjects;

import java.util.List;
import java.util.stream.Collectors;

public class IdentifierNotInitialised extends EvaluationErrorResult {
	public static final String MESSAGE = "NOT-INITIALISED";

	public IdentifierNotInitialised(final String result, final List<ErrorItem> errors) {
		super(result, errors);
	}

	public IdentifierNotInitialised(final List<String> errors) {
		this(MESSAGE, errors.stream()
			.map(ErrorItem::fromErrorMessage)
			.collect(Collectors.toList()));
	}

}
