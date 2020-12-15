package de.prob.animator.domainobjects;

/**
 * Provides constants for the names of the most commonly used dot layout engines.
 * These are all of the layout engines listed in the dot(1) man page of Graphviz 2.40.1.
 */
public final class DotLayoutEngine {
	public static final String DOT = "dot";
	public static final String NEATO = "neato";
	public static final String TWOPI = "twopi";
	public static final String CIRCO = "circo";
	public static final String FDP = "fdp";
	public static final String SFDP = "sfdp";
	public static final String PATCHWORK = "patchwork";
	public static final String OSAGE = "osage";
	
	private DotLayoutEngine() {
		throw new AssertionError("Utility class");
	}
}
