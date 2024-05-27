package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonPropertyOrder({ "predicate" })
public class PostconditionPredicate extends Postcondition {

	private String predicate;

	public PostconditionPredicate() {
		super(PostconditionKind.PREDICATE);
		this.predicate = "";
	}

	@JsonCreator
	public PostconditionPredicate(@JsonProperty("predicate") final String predicate) {
		super(PostconditionKind.PREDICATE);
		this.predicate = predicate != null ? predicate : "";
	}

	@JsonProperty("predicate")
	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate != null ? predicate : "";
	}

	@Override
	public String toString() {
		return String.format("PostconditionPredicate{predicate = %s}", predicate);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {return true;}
		if (o == null || getClass() != o.getClass()) {return false;}
		PostconditionPredicate that = (PostconditionPredicate) o;
		return Objects.equals(predicate, that.predicate);
	}

	@Override
	public int hashCode() {
		return predicate.hashCode();
	}
}
