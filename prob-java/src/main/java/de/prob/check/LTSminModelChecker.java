package de.prob.check;

import java.util.Objects;

import de.prob.animator.CommandInterruptedException;
import de.prob.animator.command.LTSminModelCheckingCommand;
import de.prob.statespace.StateSpace;

public class LTSminModelChecker extends CheckerBase {

	private final LTSminModelCheckingOptions options;

	public LTSminModelChecker(StateSpace stateSpace) {
		this(stateSpace, LTSminModelCheckingOptions.DEFAULT);
	}

	public LTSminModelChecker(StateSpace stateSpace, LTSminModelCheckingOptions options) {
		this(stateSpace, options, null);
	}

	public LTSminModelChecker(StateSpace stateSpace, LTSminModelCheckingOptions options, IModelCheckListener listener) {
		super(stateSpace, listener);
		this.options = Objects.requireNonNull(options, "options");
	}

	@Override
	protected void execute() {
		try {
			this.getStateSpace().startTransaction();

			LTSminModelCheckingCommand cmd = new LTSminModelCheckingCommand(this.options);
			try {
				this.getStateSpace().execute(cmd);
			} catch (CommandInterruptedException e) {
				this.isFinished(new CheckInterrupted(), null);
				return;
			}
			if (Thread.interrupted()) {
				this.isFinished(new CheckInterrupted(), null);
				return;
			}

			this.isFinished(cmd.getResult(), null);
		} finally {
			this.getStateSpace().endTransaction();
		}
	}
}
