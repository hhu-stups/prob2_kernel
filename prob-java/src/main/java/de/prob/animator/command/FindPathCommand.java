package de.prob.animator.command;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalElementType;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.*;

import java.util.*;

/**
 * Use RefineTraceCommand instead; Can be removed after removing dependency in UI
 */
public class FindPathCommand extends AbstractCommand implements
		IStateSpaceModifier, ITraceDescription {


	private static final String PROLOG_COMMAND_NAME = "prob2_find_trace";
	private static final String RESULT_VARIABLE = "Res";
	private static final String ERRORS_VARIABLE = "Errors";

	private final List<ClassicalB> evalElement;
	private final State stateId;
	private final List<String> name;
	private final StateSpace stateSpace;
	private final List<Transition> resultTrace = new ArrayList<>();
	private final List<String> errors = new ArrayList<>();


	/**
	 * Tries to satisfy the given path with given predicates. Will fail if path is not executable.
	 * @param s the state space - the machine to satisfy the trace on
	 * @param stateId the entry point
	 * @param trace the trace to satisfy
	 * @param predicates the constraints to put on each transition; maps 1:1 with trace
	 */
	public FindPathCommand(final StateSpace s, final State stateId, final List<String> trace, final List<ClassicalB> predicates) {
		this.stateSpace = s;
		this.stateId = stateId;
		this.name = trace;
		this.evalElement = predicates;


		if (trace.size() != predicates.size()) {
			throw new IllegalArgumentException(
					"Must provide the same number of names and predicates.");
		}
		for (ClassicalB classicalB : predicates) {
			if (!EvalElementType.PREDICATE.equals(classicalB.getKind())) {
				throw new IllegalArgumentException(
						"Formula must be a predicate, not " + classicalB.getKind() + ": " + classicalB);
			}
		}
	}


	/**
	 * This method is called when the command is prepared for sending. The
	 * method is called by the Animator class.
	 *
	 * @see de.prob.animator.command.AbstractCommand writeCommand(de.prob.prolog.output.IPrologTermOutput)
	 */
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);

		pto .printAtomOrNumber(stateId.getId());

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
		Trace t = s.getTrace(stateId.getId());
		return t.addTransitions(resultTrace);
	}

	public List<String> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}


}
