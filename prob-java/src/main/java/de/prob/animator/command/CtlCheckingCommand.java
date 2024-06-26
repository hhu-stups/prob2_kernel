/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.CTL;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.check.CTLCouldNotDecide;
import de.prob.check.CTLCounterExample;
import de.prob.check.CTLError;
import de.prob.check.CTLNotYetFinished;
import de.prob.check.CTLOk;
import de.prob.check.IModelCheckingResult;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

public final class CtlCheckingCommand extends AbstractCommand implements
		IStateSpaceModifier {
	private static final String PROLOG_COMMAND_NAME = "prob2_do_ctl_modelcheck";
	private static final String VARIABLE_NAME_RESULT = "R";
	private static final String VARIABLE_NAME_COUNTER_EXAMPLE = "CE";

	private final StateSpace s;
	private final CTL ctlFormula;
	private final int max;
	private final State startState;

	private IModelCheckingResult result;

	public CtlCheckingCommand(StateSpace s, CTL ctlFormula, int max, State startState) {
		this.s = s;
		this.ctlFormula = ctlFormula;
		this.max = max;
		this.startState = startState;
	}
	
	public CtlCheckingCommand(StateSpace s, CTL ctlFormula, int max) {
		this(s, ctlFormula, max, null);
	}

	public IModelCheckingResult getResult() {
		return result;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm res = bindings.get(VARIABLE_NAME_RESULT);
		PrologTerm counterExample = bindings.get(VARIABLE_NAME_COUNTER_EXAMPLE);

		if (res.hasFunctor("type_error", 1)) {
			final List<ErrorItem> errors = BindingGenerator.getList(res.getArgument(1)).stream()
				.map(ErrorItem::fromProlog)
				.collect(Collectors.toList());
			this.result = new CTLError(ctlFormula, errors);
		} else if (res.hasFunctor("true", 0)) {
			this.result = new CTLOk(ctlFormula);
		} else if(res.hasFunctor("false", 0)) {
			final List<Transition> transitions = BindingGenerator.getList(counterExample.getArgument(1)).stream()
					.map(term -> Transition.createTransitionFromCompoundPrologTerm(s, BindingGenerator.getCompoundTerm(term, 4)))
					.collect(Collectors.toList());
			this.result = new CTLCounterExample(s, ctlFormula, transitions);
		} else if(res.hasFunctor("incomplete", 0)) {
			this.result = new CTLNotYetFinished(ctlFormula);
		} else if(counterExample.hasFunctor("unexpected_result", 0)) {
			this.result = new CTLCouldNotDecide(ctlFormula);
		} else {
			throw new AssertionError("Unknown result from CTL checking: " + res);
		}
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		ctlFormula.printProlog(pto);

		pto.openList();
		pto.openTerm("max_new_states");
		pto.printNumber(max);
		pto.closeTerm();
		pto.openTerm("mode");
		if (startState == null) {
			pto.printAtom("init");
		} else {
			pto.openTerm("specific_node");
			pto.printAtomOrNumber(startState.getId());
			pto.closeTerm();
		}
		pto.closeTerm();
		pto.closeList();

		pto.printVariable(VARIABLE_NAME_RESULT);
		pto.printVariable(VARIABLE_NAME_COUNTER_EXAMPLE);
		pto.closeTerm();
	}

	@Override
	public List<Transition> getNewTransitions() {
		if (result instanceof CTLCounterExample) {
			return ((CTLCounterExample) result).getTransitions();
		} else {
			return Collections.emptyList();
		}
	}
}
