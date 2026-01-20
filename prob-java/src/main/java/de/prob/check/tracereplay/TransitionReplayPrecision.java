package de.prob.check.tracereplay;

import de.prob.prolog.term.PrologTerm;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TransitionReplayPrecision {
	FAILED("failed"),
	KEEP_NAME("keep_name"),
	PARAMETERS_ONLY("parameters_only"),
	PARAMETERS_AND_RESULTS("params_and_results"),
	PRECISE("precise");

	private static final Map<String, TransitionReplayPrecision> BY_FUNCTOR = Arrays.stream(values())
			.collect(Collectors.toMap(TransitionReplayPrecision::getPrologTerm,p -> p));

	private final String prologTerm;

	TransitionReplayPrecision(String prologTerm) {
		this.prologTerm = prologTerm;
	}

	public String getPrologTerm() {
		return prologTerm;
	}

	public static TransitionReplayPrecision fromPrologTerm(final PrologTerm term) {
		TransitionReplayPrecision precision = BY_FUNCTOR.get(term.getFunctor());
		if (precision == null) {
			throw new IllegalArgumentException("Unsupported trace replay precision: " + term);
		}
		return precision;
	}
}
