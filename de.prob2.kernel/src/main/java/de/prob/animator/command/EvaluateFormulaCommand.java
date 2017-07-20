package de.prob.animator.command;

import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.ProBEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the values of Classical-B Predicates and Expressions.
 * 
 * @author joy
 * 
 */
public class EvaluateFormulaCommand extends EvaluationCommand {

	private static final String PROLOG_COMMAND_NAME = "evaluate_formula";

	Logger logger = LoggerFactory.getLogger(EvaluateFormulaCommand.class);

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

		if (this.evalElement instanceof ProBEvalElement) {
			pout.openTerm("eval_typed");
			evalElement.printProlog(pout);
			pout.printAtom(evalElement.expansion().name());
		} else {
			pout.openTerm("eval");
			evalElement.printProlog(pout);
			pout.printAtom(evalElement.getKind());
			pout.printAtom(evalElement.getCode());
			pout.printAtom(evalElement.expansion().name());
		}
		pout.closeTerm();

		pout.printVariable(EVALUATE_RESULT_VARIABLE);
		pout.closeTerm();
	}

}
