package de.prob.animator.domainobjects;

/**
 * B formulas have either the type PREDICATE or EXPRESSION.
 * 
 * @author joy
 * 
 */
public enum EvalElementType {
	PREDICATE("#PREDICATE"),
	EXPRESSION("#EXPRESSION"),
	ASSIGNMENT("#ASSIGNMENT"),
	NONE("none"),
	CSP("csp"),
	;
	
	private final String prologName;
	
	private EvalElementType(final String prologName) {
		this.prologName = prologName;
	}
	
	/**
	 * @deprecated These names are no longer used on the Prolog side.
	 */
	@Deprecated
	public String getPrologName() {
		return this.prologName;
	}
}
