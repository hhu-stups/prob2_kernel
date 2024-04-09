package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import de.prob.animator.IPrologResult;
import de.prob.animator.InterruptedResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.CBCDeadlockFound;
import de.prob.check.CheckError;
import de.prob.check.CheckInterrupted;
import de.prob.check.IModelCheckingResult;
import de.prob.check.ModelCheckOk;
import de.prob.check.NotYetFinished;
import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

/**
 * This command makes ProB search for a deadlock with an optional predicate to
 * limit the search space.
 * 
 * @author plagge
 */
public class ConstraintBasedDeadlockCheckCommand extends AbstractCommand
		implements IStateSpaceModifier {
	private static final String PROLOG_COMMAND_NAME = "prob2_deadlock_freedom_check";
	private static final String RESULT_VARIABLE = "R";

	private IModelCheckingResult result;
	private String deadlockStateId;
	private Transition deadlockOperation;
	private final IEvalElement formula;
	private final List<Transition> newOps = new ArrayList<>();

	private final StateSpace s;

	/**
	 * @param predicate is a parsed predicate 
	 */
	public ConstraintBasedDeadlockCheckCommand(final StateSpace s,
			final IEvalElement predicate) {
		this.s = s;
		this.formula = predicate;
	}

	public IModelCheckingResult getResult() {
		return result;
	}

	/**
	 * @deprecated Use {@link #getResult()} instead. If a deadlock was found, the result is an instance of {@link CBCDeadlockFound}, and a trace to the deadlock state can be obtained using {@link CBCDeadlockFound#getTrace(StateSpace)}.
	 */
	@Deprecated
	public String getDeadlockStateId() {
		return deadlockStateId;
	}

	/**
	 * @deprecated Use {@link #getResult()} instead. If a deadlock was found, the result is an instance of {@link CBCDeadlockFound}, and a trace to the deadlock state can be obtained using {@link CBCDeadlockFound#getTrace(StateSpace)}.
	 */
	@Deprecated
	public Transition getDeadlockOperation() {
		return deadlockOperation;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		if (formula != null) {
			formula.printProlog(pto);
		} else {
			new ClassicalB("1=1").printProlog(pto);
		}
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		final PrologTerm resultTerm = bindings.get(RESULT_VARIABLE);
		if (resultTerm.hasFunctor("no_deadlock_found", 0)) {
			this.result = new ModelCheckOk("No deadlock was found");
		} else if (resultTerm.hasFunctor("errors", 1)) {
			PrologTerm error = resultTerm.getArgument(1);
			this.result = new CheckError("CBC Deadlock check produced errors: " + error);
		} else if (resultTerm.hasFunctor("interrupted", 0)) {
			this.result = new NotYetFinished("CBC Deadlock check was interrupted", -1);
		} else if (resultTerm.hasFunctor("deadlock", 2)) {
			CompoundPrologTerm deadlockTerm = BindingGenerator.getCompoundTerm(resultTerm, 2);

			Transition deadlockOperation = Transition.createTransitionFromCompoundPrologTerm(
				s,
				BindingGenerator.getCompoundTerm(deadlockTerm.getArgument(1), 4)
			);
			newOps.add(deadlockOperation);
			String deadlockStateId = deadlockTerm.getArgument(2).toString();

			result = new CBCDeadlockFound(deadlockStateId, deadlockOperation);
		} else {
			throw new ProBError("unexpected result from deadlock check: " + resultTerm);
		}
	}

	@Override
	public List<Transition> getNewTransitions() {
		return newOps;
	}
}
