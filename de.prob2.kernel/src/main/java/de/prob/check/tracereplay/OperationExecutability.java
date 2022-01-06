package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OperationExecutability extends Postcondition {

	protected String operation;

	protected String predicate;

	public OperationExecutability(PostconditionKind kind) {
		super(kind);
		this.operation = "";
		this.predicate = "";
	}

	public OperationExecutability(PostconditionKind kind, String operation, String predicate) {
		super(kind);
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
}
