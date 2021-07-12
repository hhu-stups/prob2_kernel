package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonPropertyOrder({
	"kind"
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "kind")
@JsonSubTypes({
	@JsonSubTypes.Type(value = PostconditionPredicate.class, name = "PREDICATE"),
	@JsonSubTypes.Type(value = OperationEnabledness.class, name = "ENABLEDNESS"),
	@JsonSubTypes.Type(value = OperationDisabledness.class, name = "DISABLEDNESS"),
})
public abstract class Postcondition {

	public enum PostconditionKind {
		PREDICATE, ENABLEDNESS, DISABLEDNESS;
	}

	private final PostconditionKind kind;

	public Postcondition(PostconditionKind kind) {
		this.kind = kind;
	}

	@JsonProperty("kind")
	public PostconditionKind getKind() {
		return kind;
	}
}
