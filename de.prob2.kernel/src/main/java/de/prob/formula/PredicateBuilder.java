package de.prob.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PredicateBuilder {

	private final List<String> predicates = new ArrayList<>();

	public PredicateBuilder() {
		//
	}

	public PredicateBuilder add(final String predicate) {
		this.predicates.add(predicate);
		return this;
	}

	public PredicateBuilder addList(List<String> predicates) {
		this.predicates.addAll(predicates);
		return this;
	}

	public PredicateBuilder add(final String name, final String value) {
		this.add(name + "=" + value);
		return this;
	}

	public PredicateBuilder addMap(Map<String, String> map) {
		map.forEach(this::add);
		return this;
	}

	@Override
	public String toString() {
		if (predicates.isEmpty()) {
			return "1=1";
		} else {
			return String.join(" & ", predicates);
		}
	}
}
