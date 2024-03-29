package de.prob.animator.domainobjects;

import java.util.List;

public class TypeCheckResult {

	private final String type;
	private final List<ErrorItem> errors;

	public TypeCheckResult(String type, List<ErrorItem> errors) {
		this.type = type;
		this.errors = errors;
	}

	public String getType() {
		return type;
	}

	public List<ErrorItem> getErrors() {
		return errors;
	}

	public boolean isOk() {
		// Type check is OK if the list of errors is empty or none of them are real errors.
		return this.getErrors().stream().noneMatch(err -> err.getType().compareTo(ErrorItem.Type.ERROR) >= 0);
	}
}
