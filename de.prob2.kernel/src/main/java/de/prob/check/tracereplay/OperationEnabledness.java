package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"operation", "predicate"})
public class OperationEnabledness extends OperationExecutability {

	public OperationEnabledness() {
		super(PostconditionKind.ENABLEDNESS);
	}

	@JsonCreator
	public OperationEnabledness(@JsonProperty("operation") final String operation,
						 		@JsonProperty("predicate") final String predicate) {
		super(PostconditionKind.ENABLEDNESS, operation, predicate);
	}

	@Override
	public String toString() {
		return String.format("OperationEnabledness{operation = %s, predicate = %s}", operation, predicate);
	}

}
