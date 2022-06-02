package de.prob.animator.domainobjects;

import de.prob.animator.command.EvaluateFormulaCommand;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.statespace.State;

/**
 * Objects that implement this interface correctly are automatically recognized
 * as a formula that can be evaluated. The user can easily get the prolog
 * representation of the given formula.
 * 
 * @author joy
 * 
 */
public interface IEvalElement {
	/**
	 * @return String representing the formula
	 */
	public abstract String getCode();

	/**
	 * Pretty-print this formula,
	 * i. e. convert the parsed AST back into equivalent source code.
	 * This can be used to check equality of two formulas while ignoring comments and formatting.
	 *
	 * @return a pretty-printed version of the formula
	 */
	public abstract String getPrettyPrint();

	/**
	 * Writes the formula to {@link IPrologTermOutput} pout
	 * 
	 * @param pout the {@link IPrologTermOutput} to write to
	 */
	public abstract void printProlog(IPrologTermOutput pout);

	/**
	 * Write the formula as a Prolog term for use in evaluation commands.
	 * 
	 * @param pout the {@link IPrologTermOutput} to write to
	 */
	default void printEvalTerm(IPrologTermOutput pout) {
		switch (this.getKind()) {
			case PREDICATE:
				pout.openTerm("bpred");
				break;
			
			case EXPRESSION:
				pout.openTerm("bexpr");
				break;
			
			default:
				throw new EvaluationException("Formula type not supported for evaluation: " + this.getKind());
		}
		this.printProlog(pout);
		pout.closeTerm();
	}

	/**
	 * @return The kind of the formula. For B formulas, this needs to be either formula or expression. For other formula types, new kinds need to be defined to recognize the formula.
	 */
	public abstract EvalElementType getKind();

	public String serialized();

	public IFormulaUUID getFormulaId();

	/**
	 * @deprecated Use {@link State#eval(IEvalElement)} or similar methods, or manually construct a {@link EvaluateFormulaCommand} if necessary.
	 */
	@Deprecated
	public EvaluateFormulaCommand getCommand(State state);

	public FormulaExpand expansion();
}
