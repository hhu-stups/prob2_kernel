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
	NONE(null),
	CSP("csp"),
	;

	private final String evalTermName;

	EvalElementType(String evalTermName) {
		this.evalTermName = evalTermName;
	}

	public String getEvalTermName() {
		return this.evalTermName;
	}
}
