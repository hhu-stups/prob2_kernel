package de.prob.animator.domainobjects;

import java.util.Objects;

/**
 * Different eval types supported by Prolog and the ProB Kernel.
 *
 * @author joy
 */
public enum EvalElementType {

	PREDICATE("bpred"),
	EXPRESSION("bexpr"),
	ASSIGNMENT("bsubst"),
	NONE("none"),
	CSP("csp"),
	;

	private final String prologName;

	EvalElementType(final String prologName) {
		this.prologName = Objects.requireNonNull(prologName, "prologName");
	}

	public String getPrologName() {
		return this.prologName;
	}
}
