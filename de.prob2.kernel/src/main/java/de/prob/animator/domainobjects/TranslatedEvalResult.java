package de.prob.animator.domainobjects;

import java.util.Map;
import java.util.Set;

import de.hhu.stups.prob.translator.BValue;

public class TranslatedEvalResult<T extends BValue> extends AbstractEvalResult {
	private final T value;
	private final Map<String, BValue> solutions;

	public TranslatedEvalResult(final T value, final Map<String, BValue> solutions) {
		super();
		this.value = value;
		this.solutions = solutions;
	}

	public Map<String, BValue> getSolutions() {
		return solutions;
	}

	/**
	 * Tries to access a solution with the given name for the result.
	 *
	 * @param name of solution
	 * @return Object representation of solution, or {@code null} if the solution does not exist
	 */
	public BValue getSolution(final String name) {
		return solutions.get(name);
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public T getValue() {
		return value;
	}

	public Set<String> getKeys() {
		return solutions.keySet();
	}
}
