package de.prob.check;

public enum ModelCheckingSearchStrategy {
	MIXED_BF_DF("mixed"),
	BREADTH_FIRST("breadth_first"),
	DEPTH_FIRST("depth_first"),
	// These are supported on the Prolog side, but not yet by the prob2_interface do_modelchecking predicate.
	HEURISTIC_FUNCTION("heuristic"),
	HASH_RANDOM("hash"),
	RANDOM("random"),
	OUT_DEGREE_HASH("out_degree_hash"),
	TERM_SIZE("term_size"),
	DISABLED_TRANSITIONS("disabled_actions"),
	;
	
	private final String prologName;
	
	private ModelCheckingSearchStrategy(final String prologName) {
		this.prologName = prologName;
	}
	
	public String getPrologName() {
		return this.prologName;
	}
}
