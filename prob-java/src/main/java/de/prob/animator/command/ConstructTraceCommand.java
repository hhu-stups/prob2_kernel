/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalElementType;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

/**
 * Command to execute an event that has not been enumerated by ProB.
 * 
 * @author Jens Bendisposto
 * 
 */
public final class ConstructTraceCommand extends AbstractCommand implements
		IStateSpaceModifier, ITraceDescription {

	private static final String PROLOG_COMMAND_NAME = "prob2_construct_trace";
	private static final String RESULT_VARIABLE = "Res";
	private static final String ERRORS_VARIABLE = "Errors";

	private final List<ClassicalB> evalElement;
	private final State state;
	private final List<String> name;
	private final StateSpace stateSpace;
	private final List<Transition> resultTrace = new ArrayList<>();
	private final List<String> errors = new ArrayList<>();
	private List<Integer> executionNumber = new ArrayList<>();

	public ConstructTraceCommand(final StateSpace s, final State state,
			final List<String> name, final List<ClassicalB> predicate,
			final Integer executionNumber) {
		this.stateSpace = s;
		this.state = state;
		this.name = name;
		this.evalElement = predicate;
		if (name.size() != predicate.size()) {
			throw new IllegalArgumentException(
					"Must provide the same number of names and predicates.");
		}
		for (ClassicalB classicalB : predicate) {
			if (!EvalElementType.PREDICATE.equals(classicalB.getKind())) {
				throw new IllegalArgumentException(
						"Formula must be a predicate, not " + classicalB.getKind() + ": " + classicalB);
			}
		}
		int size = this.name.size();
		for (int i = 0; i < size; ++i) {
			this.executionNumber.add(executionNumber);
		}
	}

	public ConstructTraceCommand(final StateSpace s, final State state,
			final List<String> name, final List<ClassicalB> predicate) {
		this(s, state, name, predicate, 1);
	}

	public ConstructTraceCommand(final StateSpace s, final State state,
			final List<String> name, final List<ClassicalB> predicate,
			final List<Integer> executionNumber) {
		this(s, state, name, predicate);
		this.executionNumber = executionNumber;
		if (name.size() != executionNumber.size()) {
			throw new IllegalArgumentException(
					"Must provide the same number of names and execution numbers.");
		}
	}

	/**
	 * This method is called when the command is prepared for sending. The
	 * method is called by the Animator class, most likely it is not interesting
	 * for other classes.
	 * 
	 * @see de.prob.animator.command.AbstractCommand#writeCommand(de.prob.prolog.output.IPrologTermOutput)
	 */
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME)
				.printAtomOrNumber(state.getId());
		pto.openList();
		for (String n : name) {
			pto.printAtom(n);
		}
		pto.closeList();
		final ASTProlog prolog = new ASTProlog(pto, null);
		pto.openList();
		for (ClassicalB cb : evalElement) {
			cb.getAst().apply(prolog);
		}
		pto.closeList();
		pto.openList();
		for (Integer n : executionNumber) {
			pto.printNumber(n);
		}
		pto.closeList();
		pto.printVariable(RESULT_VARIABLE);
		pto.printVariable(ERRORS_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		ListPrologTerm trace = BindingGenerator.getList(bindings
				.get(RESULT_VARIABLE));

		for (PrologTerm term : trace) {
			CompoundPrologTerm t = BindingGenerator.getCompoundTerm(term, 4);
			Transition operation = Transition.createTransitionFromCompoundPrologTerm(
					stateSpace, t);
			resultTrace.add(operation);
		}

		ListPrologTerm reportedErrors = BindingGenerator.getList(bindings.get(ERRORS_VARIABLE));
		for (PrologTerm prologTerm : reportedErrors) {
			this.errors.add(prologTerm.toString());
		}
	}

	@Override
	public List<Transition> getNewTransitions() {
		return resultTrace;
	}

	public State getFinalState() {
		return resultTrace.get(resultTrace.size() - 1).getDestination();
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		Trace t = s.getTrace(state.getId());
		return t.addTransitions(resultTrace);
	}

	public List<String> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

}
