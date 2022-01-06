package de.prob.animator.command;

import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

/**
 * Calculates the values of Classical-B Predicates and Expressions.
 * 
 * @author joy
 * 
 */
// After next release, merge EvaluationCommand into this class and remove the SuppressWarnings.
@SuppressWarnings("deprecation")
public class EvaluateFormulaCommand extends EvaluationCommand {

	private static final String PROLOG_COMMAND_NAME = "evaluate_formula";

	private static final String EVALUATE_RESULT_VARIABLE = "Res";

	public EvaluateFormulaCommand(final IEvalElement evalElement,
			final String id) {
		super(evalElement, id);
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {

		PrologTerm term = bindings.get(EVALUATE_RESULT_VARIABLE);

		value = EvalResult.getEvalResult(term);
	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		pout.printAtomOrNumber(stateId);

		pout.openTerm("eval");
		evalElement.printProlog(pout);
		pout.printAtom(evalElement.getKind().getPrologName());
		pout.printAtom(evalElement.getCode());
		pout.printAtom(evalElement.expansion().getPrologName());
		pout.closeTerm();

		pout.printVariable(EVALUATE_RESULT_VARIABLE);
		pout.closeTerm();
	}

}
