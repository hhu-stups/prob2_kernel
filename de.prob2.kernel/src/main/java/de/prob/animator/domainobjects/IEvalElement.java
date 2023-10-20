package de.prob.animator.domainobjects;

import java.util.Collection;

import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.statespace.StateSpace;

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

	/**
	 * Do not call this method directly.
	 * It is only used internally by the formula registration mechanism ({@link StateSpace#registerFormulas(Collection)}).
	 * This method may be removed in the future if that implementation changes.
	 * 
	 * @return a unique identifier for this formula,
	 *     different from all other formula IDs in the same ProB Java API instance
	 */
	public IFormulaUUID getFormulaId();

	/**
	 * This setting is obsolete.
	 * The preferred way to change the expansion behavior is by passing a {@link FormulaExpand} or {@link EvalOptions} parameter to the evaluation method/command.
	 * The expansion mode stored in the formula is only used as a fallback if no other setting is passed at evaluation time.
	 * 
	 * @return the default expansion mode to use when evaluating this formula
	 */
	public FormulaExpand expansion();
}
