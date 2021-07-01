package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({"kind", "value"})
public class Postcondition {

    public enum PostconditionKind {
        PREDICATE, ENABLEDNESS;
    }

    private PostconditionKind kind;

    private String value;

    public Postcondition(final PostconditionKind kind) {
        this.kind = kind;
        this.value = "";
    }

    public Postcondition(@JsonProperty("kind") final PostconditionKind kind,
                         @JsonProperty("value") final String value) {
        this.kind = kind;
        this.value = value;
    }

    @JsonProperty("kind")
    public PostconditionKind getKind() {
        return kind;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setKind(PostconditionKind kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "Postcondition{" +
                "kind=" + kind +
                ", value='" + value + '\'' +
                '}';
    }
}
