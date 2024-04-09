package de.prob.animator.domainobjects;

/**
 * Different eval types supported by Prolog and the ProB Kernel.
 *
 * @author joy
 */
public enum EvalElementType {

	PREDICATE("bpred"),
	EXPRESSION("bexpr"),
	ASSIGNMENT(null),
	NONE(null),
	CSP("csp"),
	;

	private final String evalTermName;

	EvalElementType(String evalTermName) {
		this.evalTermName = evalTermName;
	}

	String getEvalTermName() {
		return this.evalTermName;
	}
}
