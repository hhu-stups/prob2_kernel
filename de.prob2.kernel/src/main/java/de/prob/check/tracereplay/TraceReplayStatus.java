package de.prob.check.tracereplay;

import de.prob.prolog.term.PrologTerm;

public enum TraceReplayStatus {
	PARTIAL,
	IMPERFECT,
	PERFECT,
	;
	
	public static TraceReplayStatus fromPrologTerm(final PrologTerm term) {
		switch (term.getFunctor()) {
			case "partial":
				return PARTIAL;
			
			case "imperfect":
				// TODO The argument of imperfect(Grade) is not exposed yet
				return IMPERFECT;
			
			case "perfect":
				return PERFECT;
			
			default:
				throw new IllegalArgumentException("Unsupported trace replay status: " + term);
		}
	}
}
