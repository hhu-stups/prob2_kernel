package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.prob.check.CBCInvariantViolationFound;
import de.prob.check.IModelCheckingResult;
import de.prob.check.InvariantCheckCounterExample;
import de.prob.check.ModelCheckOk;
import de.prob.check.NotYetFinished;
import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

/**
 * This command makes ProB search for a invariant violation with an optional
 * selection of events.
 * 
 * @author plagge
 */
public class ConstraintBasedInvariantCheckCommand extends AbstractCommand
		implements IStateSpaceModifier {
	private static final String PROLOG_COMMAND_NAME = "prob2_invariant_check";
	private static final String RESULT_VARIABLE = "R";

	private final Collection<String> events;

	private IModelCheckingResult result;
	private final List<InvariantCheckCounterExample> counterexamples = new ArrayList<>();
	private final List<Transition> newTransitions = new ArrayList<>();

	private final StateSpace s;

	/**
	 * @param events
	 *            is a collection of names of that events that should be
	 *            checked. May be {@code null}. In that case, all events
	 *            are checked.
	 */
	public ConstraintBasedInvariantCheckCommand(final StateSpace s, final Collection<String> events) {
		this.s = s;
		this.events = events == null ? null : Collections
				.unmodifiableCollection(new ArrayList<>(events));
	}

	public Collection<String> getEvents() {
		return events;
	}

	/**
	 * @deprecated Use {@link #getResult()} instead. If any counterexamples were found, the result is an instance of {@link CBCInvariantViolationFound}, and a list of counterexamples can be obtained using {@link CBCInvariantViolationFound#getCounterexamples()}.
	 */
	@Deprecated
	public List<InvariantCheckCounterExample> getCounterExamples() {
		return counterexamples;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		if (events != null && !events.isEmpty()) {
			pto.openTerm("ops");
			pto.openList();
			for (final String event : events) {
				pto.printAtom(event);
			}
			pto.closeList();
			pto.closeTerm();
		} else {
			pto.printAtom("all");
		}
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		final PrologTerm resultTerm = bindings.get(RESULT_VARIABLE);
		if (resultTerm.hasFunctor("interrupted", 0)) {
			result = new NotYetFinished(
					"The invariant check has been interrupted by the user.", -1);

		} else if (resultTerm.isList()) {
			ListPrologTerm ceTerm = (ListPrologTerm) resultTerm;
			counterexamples.addAll(extractExamples(ceTerm));
			result = ceTerm.isEmpty() ? new ModelCheckOk(
					"No Invariant violation was found")
					: new CBCInvariantViolationFound(counterexamples);
		} else {
			throw new ProBError("unexpected result from invariant check: " + resultTerm);
		}
	}

	private List<InvariantCheckCounterExample> extractExamples(
			final ListPrologTerm ceTerm) {
		List<InvariantCheckCounterExample> examples = new ArrayList<>();
		for (final PrologTerm t : ceTerm) {
			final CompoundPrologTerm term = (CompoundPrologTerm) t;
			final String eventName = term.getArgument(1).atomToString();
			final Transition step1 = Transition.createTransitionFromCompoundPrologTerm(
				s, BindingGenerator.getCompoundTerm(term.getArgument(2), 4));
			final Transition step2 = Transition.createTransitionFromCompoundPrologTerm(
				s, BindingGenerator.getCompoundTerm(term.getArgument(3), 4));
			final InvariantCheckCounterExample ce = new InvariantCheckCounterExample(eventName, step1, step2);
			newTransitions.add(step1);
			newTransitions.add(step2);
			examples.add(ce);
		}
		return examples;
	}

	public IModelCheckingResult getResult() {
		return result;
	}

	@Override
	public List<Transition> getNewTransitions() {
		return newTransitions;
	}
}
