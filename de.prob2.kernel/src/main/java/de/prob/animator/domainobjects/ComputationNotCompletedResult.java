package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;

public class ComputationNotCompletedResult extends EvaluationErrorResult {
	private final String code;

	public ComputationNotCompletedResult(final String result, final List<String> errors, final String code) {
		super(result, errors);
		this.code = code;
	}

	public ComputationNotCompletedResult(final String result, final List<String> errors) {
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
		this("Computation not completed", Collections.singletonList(reason), code);
	}

	public String getReason() {
		return String.join(",", this.getErrors());
	}

	public String getCode() {
		return code;
	}
}
