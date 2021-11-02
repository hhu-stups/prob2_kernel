package de.prob.check.tracereplay;

import de.prob.prolog.term.PrologTerm;

public enum TransitionReplayPrecision {
	FAILED,
	PARAMETERS_ONLY,
	PARAMETERS_AND_RESULTS,
	PRECISE,
	;
	
	public static TransitionReplayPrecision fromPrologTerm(final PrologTerm term) {
		switch (term.getFunctor()) {
			case "failed":
				return FAILED;
			
			case "parameters_only":
				return PARAMETERS_ONLY;
			
			case "params_and_results":
				return PARAMETERS_AND_RESULTS;
			
			case "precise":
				return PRECISE;
			
			default:
				throw new IllegalArgumentException("Unsupported trace replay precision: " + term);
		}
	}
}
