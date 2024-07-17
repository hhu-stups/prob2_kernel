package de.prob.check;

import de.prob.animator.command.ReplayStateTraceFileCommand;
import de.prob.statespace.StateSpace;
import de.tlc4b.tlc.TLCMessageListener;
import de.tlc4b.tlc.TLCResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tlc2.output.EC;
import tlc2.output.Message;

import static tlc2.output.MP.*;

public class TLCStats extends TLCMessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TLCStats.class);

	private final TLCModelChecker tlcModelChecker;

	private StateSpaceStats lastStats = new StateSpaceStats(1,0,0);
	private boolean finished = false;

	public TLCStats(TLCModelChecker tlcModelChecker) {
		this.tlcModelChecker = tlcModelChecker;
		this.tlcModelChecker.updateStats(new NotYetFinished("TLC model checking started.", 0), lastStats);
	}

	public void onMessage(Message message) {
		if (finished)
			// once finished, ignore further updates
			return;

		if (message.getMessageClass() == NONE) { // is status message
			StateSpaceStats stats = handleStatusMessage(message);
			IModelCheckingResult result;
			if (!finished) {
				result = new NotYetFinished("TLC Model Checking not completed.",
					stats.getNrTotalNodes() - stats.getNrProcessedNodes());
				tlcModelChecker.updateStats(result, stats);
				lastStats = stats;
			}
		}
	}

	private StateSpaceStats handleStatusMessage(Message message) {
		int totalNodes = lastStats.getNrTotalNodes();
		int totalTransitions = lastStats.getNrTotalTransitions();
		int processedNodes = lastStats.getNrProcessedNodes();
		switch (message.getMessageCode()) {
			case EC.TLC_STATS:
				totalTransitions = Integer.parseInt(message.getParameters()[0]);
				totalNodes = Integer.parseInt(message.getParameters()[1]);
				processedNodes = totalNodes;
				break;
			case EC.TLC_PROGRESS_STATS:
				totalTransitions = Integer.parseInt(message.getParameters()[1]);
				totalNodes = Integer.parseInt(message.getParameters()[2]);
				processedNodes = Integer.parseInt(message.getParameters()[3]);
				break;
			case EC.TLC_SUCCESS:
				finished = true;
				break;
			// Success and error messages (violations) are handled below using the TLCResults after the check has been completed.
			default:
				break;
		}
		return new StateSpaceStats(totalNodes, totalTransitions, processedNodes);
	}

	void handleResults(final TLCResults results) {
		this.lastStats = new StateSpaceStats(results.getNumberOfDistinctStates(), results.getNumberOfTransitions(),
			results.getNumberOfDistinctStates());

		IModelCheckingResult result;
		switch (results.getTLCResult()) {
			case Deadlock:
				result = new ModelCheckErrorUncovered("Deadlock found.", getDestStateId(results));
				break;
			case Goal:
				result = new ModelCheckGoalFound("Goal found.", getDestStateId(results));
				break;
			case InvariantViolation:
				result = new ModelCheckErrorUncovered("Invariant violation found.", getDestStateId(results));
				break;
			case ParseError:
				result = new CheckError("Parse error.");
				break;
			case NoError:
				result = new ModelCheckOk("Model Checking complete. No error nodes found.");
				break;
			case AssertionError:
				result = new ModelCheckErrorUncovered("Assertion violation found.", getDestStateId(results));
				break;
			case PropertiesError:
				result = new ModelCheckErrorUncovered("Properties violation found.", getDestStateId(results));
				break;
			case EnumerationError:
				result = new CheckError("Enumeration error.");
				break;
			case TemporalPropertyViolation:
				// TODO improve handling; like LTLCounterExample?
				result = new ModelCheckErrorUncovered("LTL formula " + results.getViolatedDefinition()
					+ " is not satisfied.", getDestStateId(results));
				break;
			case WellDefinednessError:
				result = new CheckError("Welldefinedness error.");
				break;
			case TLCError:
			case InitialStateError:
			default:
				result = new CheckError("Unknown error during TLC Model Checking.");
				break;
		}

		tlcModelChecker.isFinished(result, lastStats);
	}

	private String getDestStateId(final TLCResults results) {
		StateSpace stateSpace = tlcModelChecker.getStateSpace();
		String root = String.valueOf(stateSpace.getRoot().numericalId());
		String tracePath = results.getTraceFilePath();

		String destId;
		if (tracePath != null) {
			ReplayStateTraceFileCommand command = new ReplayStateTraceFileCommand(tracePath, root);
			stateSpace.execute(command);
			destId = command.getDestStateId();
		} else {
			destId = root;
			LOGGER.info("No trace file provided in TLC results.");
		}
		return destId;
	}
}
