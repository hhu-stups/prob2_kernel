package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"operation", "predicate"})
public class OperationDisabledness extends OperationExecutability {

	public OperationDisabledness() {
		super(PostconditionKind.DISABLEDNESS);
	}

	@JsonCreator
	public OperationDisabledness(@JsonProperty("operation") final String operation,
			@JsonProperty("predicate") final String predicate) {
		super(PostconditionKind.DISABLEDNESS, operation, predicate);
	}

	@Override
	public String toString() {
		return String.format("OperationDisabledness{operation = %s, predicate = %s}", operation, predicate);
	}

}
