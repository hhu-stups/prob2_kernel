package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"predicate"})
public class PostconditionPredicate extends Postcondition {

	private String predicate;

	public PostconditionPredicate() {
		super(PostconditionKind.PREDICATE);
		this.predicate = "";
	}

	@JsonCreator
	public PostconditionPredicate(@JsonProperty("predicate") final String predicate) {
		super(PostconditionKind.PREDICATE);
		this.predicate = predicate;
	}

	@JsonProperty("predicate")
	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	@Override
	public String toString() {
		return String.format("PostconditionPredicate{predicate = %s}", predicate);
	}

}
