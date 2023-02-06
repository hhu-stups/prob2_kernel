package de.prob.animator.command;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.prob.animator.domainobjects.*;
import de.prob.check.tracereplay.check.refinement.TraceRefinementResult;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.*;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

public class RefineTraceCommand extends AbstractCommand implements
		IStateSpaceModifier, ITraceDescription {


	private static final String PROLOG_COMMAND_NAME = "prob2_refine_trace";
	private static final String RESULT_VARIABLE = "Res";


	private final List<? extends IEvalElement> eval;
	private final State stateId;
	private final List<String> name;
	private final StateSpace stateSpace;

	private final List<String> errors = new ArrayList<>();
	private final Map<String, List<String>> alternatives;
	private final List<String> refineAlternatives;
	private final List<String> skips;
	private final int maxDepth;
	private final int maxBreadth;

	private TraceRefinementResult result;

	/**
	 * Tries to satisfy the given path with given predicates. Will fail if path is not executable. Is provided with
	 * alternatives to especially explore refinements.
	 *
	 * @param s            the state space - the machine to satisfy the trace on
	 * @param stateId      the entry point
	 * @param trace        the trace to satisfy
	 * @param predicates   the constraints to put on each transition; maps 1:1 with trace
	 * @param alternatives In cases where a transition can have alternatives (e.g. 1:n refinements of Event-B), otherwise empty
	 * @param skips    All events/operations that are not introduced via a skip refinement, empty if using B
	 * @param maxDepth maximum search depth
	 * @param maxBreadth maximum search breadth
	 */
	public RefineTraceCommand(final StateSpace s, final State stateId,
							  final List<String> trace, final List<? extends IEvalElement> predicates, final Map<String, List<String>> alternatives, final List<String> refinedAlternatives, final List<String> skips, final int maxBreadth, final int maxDepth) {
		this.stateSpace = s;
		this.stateId = stateId;
		this.name = trace;
		this.alternatives = alternatives;
		this.refineAlternatives = refinedAlternatives;
		this.skips = skips;
		this.eval = predicates;
		this.maxBreadth = maxBreadth;
		this.maxDepth = maxDepth;


		if (trace.size() != predicates.size()) {
			throw new IllegalArgumentException(
					"Must provide the same number of names and predicates.");
		}
		for (IEvalElement eval : predicates) {
			if (!EvalElementType.PREDICATE.equals(eval.getKind())) {
				throw new IllegalArgumentException(
						"Formula must be a predicate, not " + eval.getKind() + ": " + eval);
			}
		}


	}

	/**
	 * Tries to satisfy the given path with given predicates. Will fail if path is not executable
	 *
	 * @param s          the state space - the machine to satisfy the trace on
	 * @param stateId    the entry point
	 * @param trace      the trace to satisfy
	 * @param predicates the constraints to put on each transition; maps 1:1 with trace
	 */
	@Deprecated
	public RefineTraceCommand(final StateSpace s, final State stateId,
							  final List<String> trace, final List<? extends IEvalElement> predicates) {

		this.stateSpace = s;
		this.stateId = stateId;
		this.name = trace;
		this.eval = predicates;
		this.alternatives = new HashSet<>(trace).stream().collect(toMap(entry -> entry, Collections::singletonList));
		this.refineAlternatives = emptyList();
		this.skips = emptyList();
		maxBreadth = 10;
		maxDepth = 5;
	}

	/**
	 * Tries to satisfy the given path with given predicates. Will fail if path is not executable. Is provided with
	 * alternatives to especially explore refinements.
	 *
	 * @param s            the state space - the machine to satisfy the trace on
	 * @param stateId      the entry point
	 * @param trace        the trace to satisfy
	 * @param predicates   the constraints to put on each transition; maps 1:1 with trace
	 * @param alternatives In cases where a transition can have alternatives (e.g. refinements)
	 *                     those are stored here, expects a 1:1 mapping else
	 * @param skips    All events/operations that are not introduced via a skip refinement
	 */
	@Deprecated
	public RefineTraceCommand(final StateSpace s, final State stateId,
							  final List<String> trace, final List<? extends IEvalElement> predicates, final Map<String, List<String>> alternatives, final List<String> refinedAlternatives, final List<String> skips) {
		this.stateSpace = s;
		this.stateId = stateId;
		this.name = trace;
		this.alternatives = alternatives;
		this.refineAlternatives = refinedAlternatives;
		this.skips = skips;
		this.eval = predicates;
		maxBreadth = 10;
		maxDepth = 5;


		if (trace.size() != predicates.size()) {
			throw new IllegalArgumentException(
					"Must provide the same number of names and predicates.");
		}
		for (IEvalElement eval : predicates) {
			if (!EvalElementType.PREDICATE.equals(eval.getKind())) {
				throw new IllegalArgumentException(
						"Formula must be a predicate, not " + eval.getKind() + ": " + eval);
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

		pto.printAtomOrNumber(stateId.getId());

		pto.openList();

		for (String n : name) {
			pto.openList();
			if(alternatives.containsKey(n)) {
				for (String entry : alternatives.get(n)) {
					pto.printAtom(entry);
				}
			}else{
				pto.printAtom(n);
			}
			pto.closeList();
		}
		pto.closeList();

		final ASTProlog prolog = new ASTProlog(pto, null);
		pto.openList();
		if(!eval.isEmpty()){
			for (IEvalElement cb : eval) {
				((IBEvalElement) cb).getAst().apply(prolog);
			}
		}
		pto.closeList();


		pto.openList();
		for(String entry : refineAlternatives){
			pto.printAtom(entry);
		}
		pto.closeList();

		pto.openList();
		for (String string : skips) {
			pto.printAtom(string);
		}
		pto.closeList();


		pto.printVariable(RESULT_VARIABLE);

		pto.printNumber(maxDepth);
		pto.printNumber(maxBreadth);

		pto.closeTerm();

	}


	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {

		PrologTerm term = bindings.get(RESULT_VARIABLE);

		ListPrologTerm trace = (ListPrologTerm) term.getArgument(1);

		final List<Transition> resultTrace = new ArrayList<>();
		for (PrologTerm traceElement : trace) {
			CompoundPrologTerm t = BindingGenerator.getCompoundTerm(traceElement, 4);
			Transition operation = Transition.createTransitionFromCompoundPrologTerm(stateSpace, t);
			resultTrace.add(operation);
		}

		//TODO add trace_fail as a viable option to the prolog code, collect best fail
		if (term.hasFunctor("trace", 1)) {
			result = new TraceRefinementResult(true, resultTrace);
		} else if (term.hasFunctor("trace_fail", 1)) {
			result = new TraceRefinementResult(false, resultTrace);

		}
	}


	public TraceRefinementResult getResult(){
		return result;
	}

	@Override
	public List<Transition> getNewTransitions() {
		return result.resultTrace;
	}



	@Override
	public Trace getTrace(final StateSpace s) {
		Trace t = s.getTrace(stateId.getId());
		return t.addTransitions(result.resultTrace);
	}

	public List<String> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

}
