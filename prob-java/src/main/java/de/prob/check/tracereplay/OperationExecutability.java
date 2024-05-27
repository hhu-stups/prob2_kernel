package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class OperationExecutability extends Postcondition {

	private String operation;
	private String predicate;

	public OperationExecutability(PostconditionKind kind) {
		this(kind, null, null);
	}

	public OperationExecutability(PostconditionKind kind, String operation, String predicate) {
		super(kind);
		this.operation = operation != null ? operation : "";
		this.predicate = predicate != null ? predicate : "";
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
		this.operation = operation != null ? operation : "";
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate != null ? predicate : "";
	}

	public void setData(String operation, String predicate) {
		setOperation(operation);
		setPredicate(predicate);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {return true;}
		// checking for class equality is important because this is the implementation for the subclasses
		if (o == null || getClass() != o.getClass()) {return false;}
		OperationExecutability that = (OperationExecutability) o;
		return Objects.equals(getOperation(), that.getOperation()) && Objects.equals(getPredicate(), that.getPredicate());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getOperation(), getPredicate());
	}

	@Override
	public String toString() {
		return String.format("%s{operation = %s, predicate = %s}", getClass().getSimpleName(), getOperation(), getPredicate());
	}
}
