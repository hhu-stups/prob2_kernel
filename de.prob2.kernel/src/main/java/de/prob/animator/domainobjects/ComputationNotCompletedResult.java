package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ComputationNotCompletedResult extends EvaluationErrorResult {
	private final String code;

	public ComputationNotCompletedResult(final String result, final List<ErrorItem> errors, final String code) {
		super(result, errors);
		this.code = code;
	}

	public ComputationNotCompletedResult(final String result, final List<ErrorItem> errors) {
		this(result, errors, "formula");
	}

	/**
	 * Old constructor for backwards compatibility.
	 * Consider using {@link #ComputationNotCompletedResult(String, List, String)} instead.
	 * 
	 * @param code the formula that could not be evaluated
	 * @param reason error message
	 */
	public ComputationNotCompletedResult(final String code, final String reason) {
		this("Computation not completed", Collections.singletonList(ErrorItem.fromErrorMessage(reason)), code);
	}

	public String getReason() {
		return this.getErrorItems().stream()
			.map(ErrorItem::getMessage)
			.collect(Collectors.joining(","));
	}

	public String getCode() {
		return code;
	}
}
