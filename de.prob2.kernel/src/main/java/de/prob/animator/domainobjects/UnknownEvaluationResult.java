package de.prob.animator.domainobjects;

import java.util.List;

public class UnknownEvaluationResult extends EvaluationErrorResult {
	public static final String MESSAGE = "UNKNOWN";

	public UnknownEvaluationResult(final List<String> errors) {
		super(MESSAGE,errors);
	}
}
