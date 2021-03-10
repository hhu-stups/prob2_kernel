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
 * specified by the user or by the default options. Communications with
 * the ProB kernel take place via the {@link ModelCheckingStepCommand}.
 * 
 * @author joy
 * 
 */
public class ConsistencyChecker extends CheckerBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsistencyChecker.class);
	private static final int TIMEOUT_MS = 500;

	private ModelCheckingLimitConfiguration limitConfiguration;
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
	public ConsistencyChecker(final StateSpace s, final ModelCheckingOptions options, final IEvalElement goal, final IModelCheckListener ui) {
		super(s, ui);
		this.limitConfiguration = new ModelCheckingLimitConfiguration(getStateSpace(), stopwatch, TIMEOUT_MS,-1, -1);
		this.options = goal == null ? options : options.checkGoal(true);
		this.goal = goal;
	}

	public ModelCheckingLimitConfiguration getLimitConfiguration() {
		return limitConfiguration;
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

		ModelCheckingStepCommand cmd = null;
		StateSpaceStats stats = null;
		try {
			this.getStateSpace().startTransaction();
			ModelCheckingOptions modifiedOptions = this.options;
			limitConfiguration.computeStateSpaceCoverage();
			do {
				limitConfiguration.updateTimeLimit();
				limitConfiguration.updateNodeLimit();
				cmd = limitConfiguration.nodesLimitSet() ? new ModelCheckingStepCommand(limitConfiguration.getMaximumNodesLeft(), limitConfiguration.getTimeout(), modifiedOptions) : new ModelCheckingStepCommand(limitConfiguration.getTimeout(), modifiedOptions);
				this.getStateSpace().execute(cmd);
				stats = cmd.getStats();
				limitConfiguration.updateStateSpaceCoverage(stats);
				if (Thread.interrupted()) {
					LOGGER.info("Consistency checker received a Java thread interrupt");
					this.isFinished(new CheckInterrupted(), stats);
					return;
				}
				this.updateStats(cmd.getResult(), stats);
				modifiedOptions = modifiedOptions.recheckExisting(false);
			} while (cmd.getResult() instanceof NotYetFinished && !limitConfiguration.isFinished());
		} finally {
			this.getStateSpace().endTransaction();
		}
		this.isFinished(cmd.getResult(), stats);
	}
}
