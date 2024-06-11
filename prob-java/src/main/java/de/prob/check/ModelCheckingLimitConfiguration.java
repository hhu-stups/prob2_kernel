package de.prob.check;

import com.google.common.base.Stopwatch;
import de.prob.animator.command.ComputeStateSpaceStatsCommand;
import de.prob.statespace.StateSpace;

class ModelCheckingLimitConfiguration {
	private final StateSpace stateSpace;
	private final Stopwatch stopwatch;
	private final int initialTimeout;
	private final int nodesLimit;
	private final int timeLimit;
	private int timeout;
	private int maximumNodesLeft;
	private boolean finished;

	private int deltaNodeProcessed;
	private int oldNodesProcessed;

	public ModelCheckingLimitConfiguration(StateSpace stateSpace, Stopwatch stopwatch, int initialTimeout, int nodesLimit, int timeLimit) {
		this.stateSpace = stateSpace;
		this.stopwatch = stopwatch;
		this.initialTimeout = initialTimeout;
		this.timeout = initialTimeout;
		this.maximumNodesLeft = nodesLimit;
		this.deltaNodeProcessed = 0;
		this.oldNodesProcessed = 0;
		this.nodesLimit = nodesLimit;
		this.timeLimit = timeLimit;
		this.finished = false;
	}

	public void computeStateSpaceCoverage() {
		if(nodesLimitSet()) {
			final ComputeStateSpaceStatsCommand stateSpaceStatsCmd = new ComputeStateSpaceStatsCommand();
			stateSpace.execute(stateSpaceStatsCmd);
			oldNodesProcessed = stateSpaceStatsCmd.getResult().getNrProcessedNodes();
		}
	}

	public void updateStateSpaceCoverage(StateSpaceStats stats) {
		if(nodesLimitSet()) {
			deltaNodeProcessed = stats.getNrProcessedNodes() - oldNodesProcessed;
			oldNodesProcessed = stats.getNrProcessedNodes();
		}
	}

	public void updateTimeLimit() {
		if(timeLimitSet()) {
			long timeoutInMs = timeLimit * 1000L - stopwatch.elapsed().toMillis();
			timeout = Math.min(initialTimeout, Math.max(0, (int) timeoutInMs));
			finished = finished || timeoutInMs < 0;
		}
	}

	public void updateNodeLimit() {
		if(nodesLimitSet()) {
			maximumNodesLeft = maximumNodesLeft - deltaNodeProcessed;
			finished = finished || maximumNodesLeft <= 0;
		}
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean nodesLimitSet() {
		return nodesLimit > 0;
	}

	public boolean timeLimitSet() {
		return timeLimit > 0;
	}

	public int getMaximumNodesLeft() {
		return maximumNodesLeft;
	}

	public int getTimeout() {
		return timeout;
	}
}
