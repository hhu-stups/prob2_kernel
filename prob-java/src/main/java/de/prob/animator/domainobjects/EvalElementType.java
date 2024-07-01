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
	/**
	 * @deprecated This type is no longer used.
	 *     It was previously returned for {@link EventB} formulas that could not be parsed.
	 *     This now throws an {@link EvaluationException} instead.
	 */
	@Deprecated
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
