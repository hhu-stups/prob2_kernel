package de.prob.check;

import de.prob.animator.command.ConstraintBasedDeadlockCheckCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.StateSpace;

/**
 * This {@link IModelCheckJob} performs constraint based deadlock checking on
 * the given {@link StateSpace} using an optional {@link IEvalElement}
 * constraint. Communication with the ProB kernel takes place via the
 * {@link ConstraintBasedDeadlockCheckCommand} command.
 * 
 * @author joy
 * 
 */
public class CBCDeadlockChecker extends CheckerBase {
	private final ConstraintBasedDeadlockCheckCommand job;

	/**
	 * Calls {@link #CBCDeadlockChecker(StateSpace, IEvalElement)} with
	 * <code>null</code> as the optional constraint.
	 * 
	 * @param s
	 *            StateSpace for which the checking should take place
	 */
	public CBCDeadlockChecker(final StateSpace s) {
		this(s, new ClassicalB("1=1"));
	}

	/**
	 * Calls
	 * {@link #CBCDeadlockChecker(StateSpace, IEvalElement, IModelCheckListener)}
	 * with <code>null</code> as the UI component
	 * 
	 * @param s
	 *            StateSpace for which the checking should take place
	 * @param constraint
	 *            {@link IEvalElement} formula constraint or <code>null</code>
	 *            if no constraint is specified
	 */
	public CBCDeadlockChecker(final StateSpace s, final IEvalElement constraint) {
		this(s, constraint, null);
	}

	/**
	 * @param s
	 *            StateSpace for which the checking should take place
	 * @param constraint
	 *            {@link IEvalElement} formula constraint or <code>null</code>
	 *            if no constraint is specified
	 * @param ui
	 *            {@link IModelCheckListener} ui component if the checker should
	 *            communicate with the UI or <code>null</code> if not.
	 */
	public CBCDeadlockChecker(final StateSpace s,
			final IEvalElement constraint, final IModelCheckListener ui) {
		super(s, ui);

		job = new ConstraintBasedDeadlockCheckCommand(s, constraint);
	}

	@Override
	protected void execute() {
		this.getStateSpace().withTransaction(() -> this.getStateSpace().execute(job));
		this.isFinished(job.getResult(), null);
	}
}
