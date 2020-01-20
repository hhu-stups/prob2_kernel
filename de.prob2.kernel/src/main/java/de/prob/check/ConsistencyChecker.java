package de.prob.check;

import de.prob.animator.command.ModelCheckingStepCommand;
import de.prob.animator.command.SetBGoalCommand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.exception.ProBError;
import de.prob.statespace.StateSpace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link IModelCheckJob} performs consistency checking on a given
 * {@link StateSpace} based on the specified {@link ModelCheckingOptions}
 * specified by the user or by the default options. This class should be used
 * with the {@link ModelChecker} wrapper class in order to perform model
 * checking. Communications with the ProB kernel take place via the
 * {@link ModelCheckingStepCommand}.
 * 
 * @author joy
 * 
 */
public class ConsistencyChecker extends CheckerBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsistencyChecker.class);
	private static final int TIMEOUT_MS = 500;

	private final ModelCheckingOptions options;
	private final IEvalElement goal;

	/**
	 * calls {@link #ConsistencyChecker(StateSpace, ModelCheckingOptions)} with
	 * default model checking options ({@link ModelCheckingOptions#DEFAULT})
	 * 
	 * @param s
	 *            {@link StateSpace} in which to perform the consistency
	 *            checking
	 */
	public ConsistencyChecker(final StateSpace s) {
		this(s, ModelCheckingOptions.DEFAULT);
	}

	/**
	 * calls {@link #ConsistencyChecker(StateSpace, ModelCheckingOptions)} with
	 * null for UI
	 * 
	 * @param s
	 *            {@link StateSpace} in which to perform the consistency
	 *            checking
	 * @param options
	 *            {@link ModelCheckingOptions} specified by user
	 */
	public ConsistencyChecker(final StateSpace s, final ModelCheckingOptions options) {
		this(s, options, null);
	}

	public ConsistencyChecker(final StateSpace s, final ModelCheckingOptions options, final IEvalElement goal) {
		this(s, options, goal, null);
	}

	/**
	 * @param s
	 *            {@link StateSpace} in which to perform the consistency
	 *            checking
	 * @param options
	 *            {@link ModelCheckingOptions} specified by the user
	 * @param ui
	 *            {@link IModelCheckListener} if the UI should be informed of
	 *            updates. Otherwise, null.
	 */
	public ConsistencyChecker(final StateSpace s, final ModelCheckingOptions options, final IEvalElement goal,
			final IModelCheckListener ui) {
		super(s, ui);

		this.options = options;
		this.goal = goal;
	}

	@Override
	protected void execute() {
		if (goal != null) {
			try {
				SetBGoalCommand cmd = new SetBGoalCommand(goal);
				this.getStateSpace().execute(cmd);
			} catch (ProBError e) {
				this.isFinished(new CheckError("Type error in specified goal."), null);
				return;
			}
		}

		ModelCheckingStepCommand cmd;
		try {
			this.getStateSpace().startTransaction();
			boolean firstIteration = true;
			do {
				cmd = new ModelCheckingStepCommand(TIMEOUT_MS, this.options.recheckExisting(firstIteration));
				this.getStateSpace().execute(cmd);
				if (Thread.interrupted()) {
					LOGGER.info("Consistency checker received a Java thread interrupt");
					this.isFinished(new CheckInterrupted(), cmd.getStats());
					return;
				}
				this.updateStats(cmd.getResult(), cmd.getStats());
				firstIteration = false;
			} while (cmd.getResult() instanceof NotYetFinished);
		} finally {
			this.getStateSpace().endTransaction();
		}
		this.isFinished(cmd.getResult(), cmd.getStats());
	}

	/**
	 * Provides a way to generate a {@link ModelChecker} with consistency
	 * checking capabilities given a {@link StateSpace}. Default options will be
	 * used.
	 * 
	 * @param s
	 *            {@link StateSpace} for which the consistency checking should
	 *            take place
	 * @return {@link ModelChecker} with consistency checking capabilities.
	 */
	public static ModelChecker create(final StateSpace s) {
		return new ModelChecker(new ConsistencyChecker(s));
	}

	/**
	 * Provides a way to generate a {@link ModelChecker} with consistency
	 * checking capabilities given a {@link StateSpace} and user defined
	 * {@link ModelCheckingOptions}.
	 * 
	 * @param s
	 *            {@link StateSpace} for which the consistency checking should
	 *            take place
	 * @param options
	 *            {@link ModelCheckingOptions} specified by the user
	 * @return {@link ModelChecker} with consistency checking capabilities
	 */
	public static ModelChecker create(final StateSpace s, final ModelCheckingOptions options) {
		return new ModelChecker(new ConsistencyChecker(s, options));
	}

}
