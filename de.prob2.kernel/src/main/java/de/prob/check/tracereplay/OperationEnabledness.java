package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"operation", "predicate"})
public class OperationEnabledness extends Postcondition {

	private String operation;

	private String predicate;

	public OperationEnabledness() {
		super(PostconditionKind.ENABLEDNESS);
		this.operation = "";
		this.predicate = "";
	}

	@JsonCreator
	public OperationEnabledness(@JsonProperty("operation") final String operation,
						 		@JsonProperty("predicate") final String predicate) {
		super(PostconditionKind.ENABLEDNESS);
		this.operation = operation;
		this.predicate = predicate;
	}

	@JsonProperty("operation")
	public String getOperation() {
		return operation;
	}

	@JsonProperty("predicate")
	public String getPredicate() {
		return predicate;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public void setData(String operation, String predicate) {
		setOperation(operation);
		setPredicate(predicate);
	}

	@Override
	public String toString() {
		return String.format("OperationEnabledness{operation = %s, predicate = %s}", operation, predicate);
	}

}
