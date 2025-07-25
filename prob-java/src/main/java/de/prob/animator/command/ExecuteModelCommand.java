package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public class ExecuteModelCommand extends AbstractCommand implements IStateSpaceModifier, ITraceDescription {

	private static final String PROLOG_COMMAND_NAME = "execute_model";
	private static final String TRANSITION_VARIABLE = "Transition";
	private static final String RESULT_VARIABLE = "Result";
	private static final String EXECUTED_STEPS_VARIABLE = "Steps";
	private static final String CONTINUE_AFTER_ERRORS = "continue_after_errors";
	private static final String RETURN_TRACE = "return_trace";
	private static final String TIMEOUT = "timeout";

	private final List<Transition> resultTrace = new ArrayList<>();
	private final State startstate;
	private final StateSpace statespace;
	private int stepsExecuted;
	private final int maxNrSteps;
	private ExecuteModelResult result;
	private final boolean continueAfterErrors;
	private final boolean returnTrace;
	private final Integer timeoutInMS;

	public enum ExecuteModelResult {
		MAXIMUM_NR_OF_STEPS_REACHED, DEADLOCK, ERROR, INTERNAL_ERROR, TIME_OUT
	}

	public ExecuteModelCommand(final StateSpace statespace, final State startState, final int maxNrSteps,
	                           final boolean continueAfterErrors, final Integer timeoutInMS) {
		this(statespace, startState, maxNrSteps, continueAfterErrors, false, timeoutInMS);
	}

	public ExecuteModelCommand(final StateSpace statespace, final State startState, final int maxNrSteps,
			final boolean continueAfterErrors, final boolean returnTrace, final Integer timeoutInMS) {
		this.statespace = statespace;
		this.startstate = startState;
		this.maxNrSteps = maxNrSteps;
		this.continueAfterErrors = continueAfterErrors;
		this.returnTrace = returnTrace;
		this.timeoutInMS = timeoutInMS;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		pout.printAtomOrNumber(this.startstate.getId());
		pout.printAtomOrNumber(String.valueOf(maxNrSteps));
		// options
		pout.openList();
		if (continueAfterErrors) {
			pout.printAtom(CONTINUE_AFTER_ERRORS);
		}
		if (returnTrace) {
			pout.printAtom(RETURN_TRACE);
		}
		if (timeoutInMS != null) {
			pout.openTerm(TIMEOUT);
			pout.printNumber(timeoutInMS);
			pout.closeTerm();
		}
		pout.closeList();
		pout.printVariable(TRANSITION_VARIABLE);
		pout.printVariable(EXECUTED_STEPS_VARIABLE);
		pout.printVariable(RESULT_VARIABLE);
		pout.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		String resultTermFunctor = bindings.get(RESULT_VARIABLE).getFunctor();
		switch (resultTermFunctor) {
			case "maximum_nr_of_steps_reached":
				this.result = ExecuteModelResult.MAXIMUM_NR_OF_STEPS_REACHED;
				break;
			case "deadlock":
				this.result = ExecuteModelResult.DEADLOCK;
				break;
			case "error":
				this.result = ExecuteModelResult.ERROR;
				break;
			case "internal_error":
				this.result = ExecuteModelResult.INTERNAL_ERROR;
				break;
			case "time_out":
				this.result = ExecuteModelResult.TIME_OUT;
				break;
			default:
				throw new AssertionError("Unexpected result of execute command: " + resultTermFunctor);
		}

		PrologTerm prologTerm = bindings.get(TRANSITION_VARIABLE);
		if (prologTerm.getFunctor().equals("none")) {
			this.stepsExecuted = 0;
			return;
		}

		List<PrologTerm> terms = BindingGenerator.getList(prologTerm);
		resultTrace.addAll(terms.stream()
				.map(t -> BindingGenerator.getCompoundTerm(t, "op", 4))
				.map(t -> Transition.createTransitionFromCompoundPrologTerm(statespace, t))
				.collect(Collectors.toList()));

		AIntegerPrologTerm intPrologTerm = BindingGenerator.getAInteger(bindings.get(EXECUTED_STEPS_VARIABLE));
		stepsExecuted = intPrologTerm.intValueExact();
	}

	@Override
	public List<Transition> getNewTransitions() {
		return resultTrace;
	}

	public String getSingleTransitionName() {
		if (resultTrace.size() == 1) {
			return resultTrace.get(0).getName();
		} else {
			return null;
		}
	}

	public State getFinalState() {
		if (resultTrace.isEmpty()) {
			return this.startstate;
		} else {
			return resultTrace.get(resultTrace.size() - 1).getDestination();
		}
	}

	@Override
	public Trace getTrace(final StateSpace s) {
		Trace t = s.getTrace(startstate.getId());
		return t.addTransitions(resultTrace);
	}

	public int getNumberOfStatesExecuted() {
		return this.stepsExecuted;
	}

	/**
	 * @return if a deadlock was uncovered before the maximum number of steps
	 *         was reached.
	 */
	public boolean isDeadlocked() {
		return result.equals(ExecuteModelResult.DEADLOCK);
	}

	/**
	 * @return the result of the executeModelCommand. The result is
	 *         either {@link ExecuteModelResult#MAXIMUM_NR_OF_STEPS_REACHED} or
	 *         {@link ExecuteModelResult#DEADLOCK}.
	 */

	public ExecuteModelResult getResult() {
		return this.result;
	}
}
