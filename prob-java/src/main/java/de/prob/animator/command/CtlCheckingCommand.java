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
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CtlCheckingCommand extends AbstractCommand implements
		IStateSpaceModifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(CtlCheckingCommand.class);

	private static final String PROLOG_COMMAND_NAME = "prob2_do_ctl_modelcheck";
	private static final String VARIABLE_NAME_RESULT = "R";
	private static final String VARIABLE_NAME_COUNTER_EXAMPLE = "CE";
	private static final String VARIABLE_NAME_ERRORS = "Errors";

	private final int max;
	private IModelCheckingResult result;
	private final CTL ctlFormula;
	private final StateSpace s;

	public CtlCheckingCommand(final StateSpace s, final CTL ctlFormula, final int max) {
		this.s = s;
		this.ctlFormula = ctlFormula;
		this.max = max;
	}

	public IModelCheckingResult getResult() {
		return result;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm res = bindings.get(VARIABLE_NAME_RESULT);
		PrologTerm counterExample = bindings.get(VARIABLE_NAME_COUNTER_EXAMPLE);
		ListPrologTerm errorTerm = BindingGenerator.getList(bindings, VARIABLE_NAME_ERRORS);

		if (!errorTerm.isEmpty()) {
			if (!res.hasFunctor("typeerror", 0)) {
				LOGGER.warn("CTL checker returned errors together with a non-error result {}/{}", res.getFunctor(), res.getArity());
			}
			final List<ErrorItem> errors = errorTerm.stream()
				.map(ErrorItem::fromProlog)
				.collect(Collectors.toList());
			this.result = new CTLError(ctlFormula, errors);
		} else if (res.hasFunctor("typeerror", 0)) {
			assert errorTerm.isEmpty(); // non-empty case already handled in previous branch
			LOGGER.warn("CTL checker returned typeerror/0 result with an empty list of errors");
			this.result = new CTLError(ctlFormula, Collections.emptyList());
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
		pto.printNumber(max);
		pto.printAtom("init");
		pto.printVariable(VARIABLE_NAME_RESULT);
		pto.printVariable(VARIABLE_NAME_COUNTER_EXAMPLE);
		pto.printVariable(VARIABLE_NAME_ERRORS);
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
