package de.prob.animator.domainobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EvaluationErrorResult extends AbstractEvalResult {

	private final String result;
	private final List<ErrorItem> errors;

	/**
	 * @param result a short identifier describing the type of error
	 * @param errors all errors that occurred during evaluation
	 */
	protected EvaluationErrorResult(final String result, final List<ErrorItem> errors) {
		super();
		this.result = result;
		this.errors = new ArrayList<>(errors);
	}

	public String getResult() {
		return result;
	}

	public List<ErrorItem> getErrorItems() {
		return Collections.unmodifiableList(this.errors);
	}

	/**
	 * @deprecated Use {@link #getErrorItems()} instead to get full error information.
	 */
	@Deprecated
	public List<String> getErrors() {
		return this.getErrorItems().stream()
			.map(ErrorItem::getMessage)
			.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getResult());
		if (!this.getErrorItems().isEmpty()) {
			sb.append(": ");
			for (final ErrorItem error : this.getErrorItems()) {
				sb.append('\n');
				sb.append(error.toString());
			}
		}
		return sb.toString();
	}

}
