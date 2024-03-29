package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.ITraceDescription;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

/**
 * TODO: Wrong comment text
 * This command makes ProB search for a deadlock with an optional predicate to
 * limit the search space.
 * 
 * @author plagge
 */
public class ConstraintBasedAssertionCheckCommand extends AbstractCommand
		implements IStateSpaceModifier, ITraceDescription {

	public enum CheckingType {
		STATIC, DYNAMIC
	}

	public enum ResultType {
		INTERRUPTED, COUNTER_EXAMPLE, NO_COUNTER_EXAMPLE_FOUND, NO_COUNTER_EXAMPLE_EXISTS
	}

	private static final String COMMAND_NAME_STATIC = "cbc_static_assertion_violation_checking";
	private static final String COMMAND_NAME_DYNAMIC = "cbc_dynamic_assertion_violation_checking";
	private static final String RESULT_VARIABLE = "R";

	private final CheckingType checkingType;

	private ResultType result;
	private Transition counterExampleOperation;
	private String counterExampleStateID;
	private final StateSpace s;
	private final List<Transition> newOps = new ArrayList<>();

	public ConstraintBasedAssertionCheckCommand(final CheckingType checkingType, final StateSpace s) {
		this.checkingType = checkingType;
		this.s = s;
	}

	public ResultType getResult() {
		return result;
	}

	public Transition getCounterExampleOperation() {
		return counterExampleOperation;
	}

	public String getCounterExampleStateID() {
		return counterExampleStateID;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		if(checkingType == CheckingType.STATIC) {
			pto.openTerm(COMMAND_NAME_STATIC);
		} else {
			pto.openTerm(COMMAND_NAME_DYNAMIC);
		}
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		final PrologTerm resultTerm = bindings.get(RESULT_VARIABLE);
		if (resultTerm.hasFunctor("interrupted", 0)) {
			this.result = ResultType.INTERRUPTED;
		} else if (resultTerm.hasFunctor("no_counterexample_found", 0)) {
			this.result = ResultType.NO_COUNTER_EXAMPLE_FOUND;
		}  else if (resultTerm.hasFunctor("no_counterexample_exists", 0)) {
			result = ResultType.NO_COUNTER_EXAMPLE_EXISTS;
		} else if (resultTerm.hasFunctor("counterexample_found", 2)) {
			this.result = ResultType.COUNTER_EXAMPLE;
			CompoundPrologTerm counterExampleTerm = (CompoundPrologTerm) resultTerm;
			counterExampleOperation = Transition.createTransitionFromCompoundPrologTerm(
				s,
				BindingGenerator.getCompoundTerm(counterExampleTerm.getArgument(1), 8)
			);
			newOps.add(counterExampleOperation);
			counterExampleStateID = counterExampleTerm.getArgument(2).toString();
		} else {
			throw new ProBError("unexpected result from assertion check: " + resultTerm);
		}
	}

	@Override
	public List<Transition> getNewTransitions() {
		return newOps;
	}
	
	@Override
	public Trace getTrace(StateSpace s) {
		if(counterExampleStateID != null && result == ResultType.COUNTER_EXAMPLE) {
			return s.getTrace(counterExampleStateID);
		}
		return null;
	}
}
