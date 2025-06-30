package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

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

	private final String stateId;
	private final State state;
	private final EvalOptions options;

	public EvaluateFormulasCommand(final List<? extends IEvalElement> evalElements, final State state, final EvalOptions options) {
		this.evalElements = evalElements;
		this.stateId = state.getId();
		this.state = state;
		this.options = options;
	}

	/**
	 * @deprecated If this constructor is used, the evaluation will not benefit from registered formulas (see {@link StateSpace#registerFormulas(Collection)}.
	 *     Use {@link #EvaluateFormulasCommand(List, State, EvalOptions)} instead.
	 */
	@Deprecated
	public EvaluateFormulasCommand(final List<? extends IEvalElement> evalElements, final String stateId) {
		this.evalElements = evalElements;
		this.stateId = stateId;
		this.state = null;
		this.options = EvalOptions.DEFAULT.withExpandFromFormulas(evalElements);
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
		final Set<IEvalElement> registered;
		if (state == null) {
			registered = Collections.emptySet();
		} else {
			registered = state.getStateSpace().getRegisteredFormulas();
		}

		pout.openTerm(PROLOG_COMMAND_NAME);

		pout.openList();
		for (IEvalElement evalElement : evalElements) {
			if (registered.contains(evalElement)) {
				pout.openTerm("registered");
				evalElement.getFormulaId().printUUID(pout);
				pout.closeTerm();
			} else {
				evalElement.printEvalTerm(pout);
			}
		}
		pout.closeList();

		// Options
		pout.openList();

		pout.openTerm("state");
		pout.printAtomOrNumber(this.stateId);
		pout.closeTerm();

		this.options.printProlog(pout);

		pout.closeList();

		pout.printVariable(EVALUATE_RESULT_VARIABLE);
		pout.closeTerm();
	}

	public String getStateId() {
		return this.stateId;
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
