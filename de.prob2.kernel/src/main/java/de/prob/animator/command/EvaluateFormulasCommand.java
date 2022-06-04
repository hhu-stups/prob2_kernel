package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * Calculates the values of Classical-B Predicates and Expressions.
 * 
 * @author joy
 * 
 */
public class EvaluateFormulasCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_evaluate_formulas";

	private static final String EVALUATE_RESULT_VARIABLE = "Res";

	private final List<? extends IEvalElement> evalElements;
	private final List<AbstractEvalResult> values = new ArrayList<>();

	private String stateId;
	private final EvalOptions options;

	public EvaluateFormulasCommand(final List<? extends IEvalElement> evalElements, final String stateId, final EvalOptions options) {
		this.evalElements = evalElements;
		this.stateId = stateId;
		this.options = options;
	}

	public EvaluateFormulasCommand(final List<? extends IEvalElement> evalElements, final String stateId) {
		this(evalElements, stateId, EvalOptions.DEFAULT.withExpandFromFormulas(evalElements));
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {

		ListPrologTerm terms = BindingGenerator.getList(bindings, EVALUATE_RESULT_VARIABLE);
		for (PrologTerm term : terms) {
			values.add(EvalResult.getEvalResult(term));
		}
	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);

		pout.openList();
		for (IEvalElement evalElement : evalElements) {
			evalElement.printEvalTerm(pout);
		}
		pout.closeList();

		// Options
		pout.openList();

		pout.openTerm("state");
		pout.printAtomOrNumber(this.stateId);
		pout.closeTerm();

		pout.openTerm("truncate");
		pout.printAtom(this.options.getExpand().getPrologName());
		pout.closeTerm();

		pout.openTerm("translation_mode");
		pout.printAtom(this.options.getMode().getPrologName());
		pout.closeTerm();

		pout.closeList();

		pout.printVariable(EVALUATE_RESULT_VARIABLE);
		pout.closeTerm();
	}

	public List<AbstractEvalResult> getValues() {
		return Collections.unmodifiableList(values);
	}

	public Map<IEvalElement, AbstractEvalResult> getResultMap() {
		Map<IEvalElement, AbstractEvalResult> result = new LinkedHashMap<>();
		for (int i = 0; i < evalElements.size(); i++) {
			result.put(evalElements.get(i), values.get(i));
		}
		return Collections.unmodifiableMap(result);
	}

}
