package de.prob.check.tracereplay;

import java.nio.file.Path;

import de.prob.animator.command.ReplayTraceFileCommand;
import de.prob.exception.ProBError;
import de.prob.statespace.StateSpace;

public class TraceReplay {
	/**
	 * Replay a ProB 2 (JSON) trace file.
	 * 
	 * @param stateSpace the state space in which to replay the trace
	 * @param traceFile path of the trace file to replay
	 * @return the replayed trace and status information about the replay
	 */
	public static ReplayedTrace replayTraceFile(final StateSpace stateSpace, final Path traceFile) {
		final ReplayTraceFileCommand cmd = new ReplayTraceFileCommand(traceFile.toString());
		try {
			stateSpace.execute(cmd);
			return cmd.getTrace();
		} catch (ProBError e) {
			if (cmd.getTrace() == null) {
				throw e;
			} else {
				return cmd.getTrace().withErrors(e.getErrors());
			}
		}
	}
}
