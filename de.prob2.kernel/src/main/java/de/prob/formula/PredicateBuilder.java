package de.prob.formula;

import java.util.*;

import de.prob.animator.domainobjects.Join;

// TODO The purpose of this class is similar to Join - should they be merged?
/**
 * Creates a conjunction of predicates incrementally,
 * from other predicates or from name/value pairs.
 * 
 * @see Join
 */
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

	public Set<String> predicatesAsSet(){
		return new HashSet<>(predicates);
	}


	@Override
	public boolean equals(Object obj) {
		return obj instanceof PredicateBuilder && predicatesAsSet().containsAll(((PredicateBuilder) obj).predicatesAsSet()) && ((PredicateBuilder) obj).predicatesAsSet().containsAll(predicatesAsSet());
	}
}
