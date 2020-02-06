package de.prob.check;

import java.util.List;

import de.prob.animator.command.ConstraintBasedInvariantCheckCommand;
import de.prob.statespace.StateSpace;

/**
 * This {@link IModelCheckJob} performs constraint based invariant checking on a
 * {@link StateSpace} given an (optional) list of events to check.
 * Communications with the ProB kernel take place via the
 * {@link ConstraintBasedInvariantCheckCommand}.
 * 
 * @author joy
 * 
 */
public class CBCInvariantChecker extends CheckerBase {
	private final ConstraintBasedInvariantCheckCommand command;

	/**
	 * Calls {@link #CBCInvariantChecker(StateSpace, List)} with null as the
	 * second parameter.
	 * 
	 * @param s
	 *            StateSpace which should be checked with this
	 *            {@link CBCInvariantChecker}
	 */
	public CBCInvariantChecker(final StateSpace s) {
		this(s, null);
	}

	/**
	 * Calls {@link #CBCInvariantChecker(StateSpace, List, IModelCheckListener)}
	 * with null for the UI element
	 * 
	 * @param s
	 *            StateSpace which should be checked with this
	 *            {@link CBCInvariantChecker}
	 * @param eventNames
	 *            List of events that are to be checked or <code>null</code> if
	 *            they are all to be checked
	 */
	public CBCInvariantChecker(final StateSpace s, final List<String> eventNames) {
		this(s, eventNames, null);
	}

	/**
	 * @param s
	 *            StateSpace which should be checked with this
	 *            {@link CBCInvariantChecker}
	 * @param eventNames
	 *            List of events which are to be checked or <code>null</code> if
	 *            they are all to be checked
	 * @param ui
	 *            {@link IModelCheckListener} object if the UI should be notified of
	 *            changes, or null if not
	 */
	public CBCInvariantChecker(final StateSpace s,
			final List<String> eventNames, final IModelCheckListener ui) {
		super(s, ui);

		command = new ConstraintBasedInvariantCheckCommand(s, eventNames);
	}

	@Override
	protected void execute() {
		this.getStateSpace().withTransaction(() -> this.getStateSpace().execute(command));
		this.isFinished(command.getResult(), null);
	}
}
