package de.prob.model.brules;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.prob.animator.command.ExecuteModelCommand;
import de.prob.model.representation.AbstractModel;
import de.prob.scripting.ExtractedModel;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the following actions:
 * 
 * <pre>
 * 1) loads an ExtractedModel 
 * 2) run the execute command.
 * </pre>
 * 
 * The final state of the probcli execute run is stored. Moreover, all errors
 * which can occur while loading a model are stored. Note, that RULES projects
 * are not parsed and checked by this class. This is done before entering this
 * class.
 * 
 **/
public class ExecuteRun {

	private StateSpace stateSpace;
	private int maxNumberOfStatesToBeExecuted = Integer.MAX_VALUE;
	private Integer timeout = null;
	private final boolean continueAfterErrors;
	private final ExtractedModel<? extends AbstractModel> extractedModel;
	private final Map<String, String> prefs;
	private ExecuteModelCommand executeModelCommand;
	private State rootState;

	public ExecuteRun(final ExtractedModel<? extends AbstractModel> extractedModel, Map<String, String> prefs,
			boolean continueAfterErrors, StateSpace stateSpace) {
		this.extractedModel = extractedModel;
		this.continueAfterErrors = continueAfterErrors;
		this.prefs = prefs;
		this.stateSpace = stateSpace;
	}

	public void start() {
		final Logger logger = LoggerFactory.getLogger(getClass());
		final Stopwatch loadStopwatch = Stopwatch.createStarted();
		getOrCreateStateSpace();
		loadStopwatch.stop();
		logger.info("Time to load model: {} ms", loadStopwatch.elapsed(TimeUnit.MILLISECONDS));

		final Stopwatch executeStopwatch = Stopwatch.createStarted();
		executeModel(this.stateSpace);
		executeStopwatch.stop();
		logger.info("Time to run execute command: {} ms", executeStopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

	private void getOrCreateStateSpace() {
		if (stateSpace == null || stateSpace.isKilled()) {
			/*
			 * create a new state space if there is no previous one or if the
			 * previous state space has been killed due to a ProBError
			 */
			this.stateSpace = this.extractedModel.load(this.prefs);
		} else {
			this.stateSpace.changePreferences(this.prefs);
			this.extractedModel.loadIntoStateSpace(this.stateSpace);
		}
	}

	private void executeModel(final StateSpace stateSpace) {
		Trace t = new Trace(stateSpace);
		this.rootState = t.getCurrentState();
		executeModelCommand = new ExecuteModelCommand(stateSpace, t.getCurrentState(), maxNumberOfStatesToBeExecuted,
				continueAfterErrors, timeout);
		stateSpace.execute(executeModelCommand);
	}

	public ExecuteModelCommand getExecuteModelCommand() {
		return this.executeModelCommand;
	}

	public State getRootState() {
		return this.rootState;
	}

	public StateSpace getUsedStateSpace() {
		return this.stateSpace;
	}

}
