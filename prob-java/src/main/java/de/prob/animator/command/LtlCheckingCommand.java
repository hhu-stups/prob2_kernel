/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.animator.domainobjects.LTL;
import de.prob.check.IModelCheckingResult;
import de.prob.check.LTLCounterExample;
import de.prob.check.LTLError;
import de.prob.check.LTLNotYetFinished;
import de.prob.check.LTLOk;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LtlCheckingCommand extends AbstractCommand implements
		IStateSpaceModifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(LtlCheckingCommand.class);

	private static final String PROLOG_COMMAND_NAME = "prob2_do_ltl_modelcheck";
	private static final String VARIABLE_NAME_RESULT = "R";
	private static final String VARIABLE_NAME_ERRORS = "Errors";

	public enum PathType {
		INFINITE, FINITE, REDUCED
	}

	private final int max;
	private IModelCheckingResult result;
	private final LTL ltlFormula;
	private final StateSpace s;

	public LtlCheckingCommand(final StateSpace s, final LTL ltlFormula, final int max) {
		this.s = s;
		this.ltlFormula = ltlFormula;
		this.max = max;
	}

	public IModelCheckingResult getResult() {
		return result;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm term = bindings.get(VARIABLE_NAME_RESULT);
		ListPrologTerm errorTerm = BindingGenerator.getList(bindings, VARIABLE_NAME_ERRORS);

		if (!errorTerm.isEmpty()) {
			if (!term.hasFunctor("typeerror", 0)) {
				LOGGER.warn("LTL checker returned errors together with a non-error result {}/{}", term.getFunctor(), term.getArity());
			}
			final List<ErrorItem> errors = errorTerm.stream()
				.map(ErrorItem::fromProlog)
				.collect(Collectors.toList());
			result = new LTLError(ltlFormula, errors);
		} else if (term.hasFunctor("typeerror", 0)) {
			assert errorTerm.isEmpty(); // non-empty case already handled in previous branch
			LOGGER.warn("LTL checker returned typeerror/0 result with an empty list of errors");
			result = new LTLError(ltlFormula, Collections.emptyList());
		} else if (term.hasFunctor("ok", 0)) {
			result = new LTLOk(ltlFormula);
		} else if (term.hasFunctor("nostart", 0)) {
			result = new LTLError(ltlFormula,
					"Could not find initialisation. Try to animating the model.");
		} else if (term.hasFunctor("incomplete", 0)) {
			result = new LTLNotYetFinished(ltlFormula);
		} else if (term.hasFunctor("counterexample", 3)) {
			CompoundPrologTerm cpt = BindingGenerator.getCompoundTerm(term, 3);
			List<Transition> counterExample = BindingGenerator.getList(cpt.getArgument(1)).stream()
				.filter(pt -> !pt.hasFunctor("none", 0))
				.map(pt -> Transition.createTransitionFromCompoundPrologTerm(
					s, BindingGenerator.getCompoundTerm(pt, 4)))
				.collect(Collectors.toList());
			
			PathType pathType;
			int loopEntry;
			PrologTerm loopStatus = cpt.getArgument(2);
			if (loopStatus.hasFunctor("no_loop", 0)) {
				pathType = PathType.REDUCED;
				loopEntry = -1;
			} else if (loopStatus.hasFunctor("deadlock", 0)) {
				pathType = PathType.FINITE;
				loopEntry = -1;
			} else if (loopStatus.hasFunctor("loop", 1)) {
				pathType = PathType.INFINITE;
				loopEntry = ((AIntegerPrologTerm) loopStatus.getArgument(1)).intValueExact();
			} else {
				throw new AssertionError("LTL model check returned unexpected loop status: " + loopStatus);
			}
			
			List<Transition> pathToCE = BindingGenerator.getList(cpt.getArgument(3)).stream()
				.map(pt -> Transition.createTransitionFromCompoundPrologTerm(
					s, BindingGenerator.getCompoundTerm(pt, 4)))
				.collect(Collectors.toList());
			
			result = new LTLCounterExample(ltlFormula, s, pathToCE, counterExample, loopEntry, pathType);
		} else {
			throw new AssertionError("Unknown result from LTL checking: " + term);
		}
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		this.ltlFormula.printProlog(pto);
		pto.printNumber(max);
		pto.printVariable(VARIABLE_NAME_RESULT);
		pto.printVariable(VARIABLE_NAME_ERRORS);
		pto.closeTerm();
	}

	@Override
	public List<Transition> getNewTransitions() {
		if (result instanceof LTLCounterExample) {
			return ((LTLCounterExample) result).getOpList();
		} else {
			return Collections.emptyList();
		}
	}
}
