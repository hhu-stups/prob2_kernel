package de.prob.model.brules;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.ExecuteModelCommand;
import de.prob.model.representation.AbstractModel;
import de.prob.scripting.ExtractedModel;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteRun.class);

	private final ReusableAnimator animator;
	private static final int MAX_NUMBER_OF_STATES_TO_BE_EXECUTED = Integer.MAX_VALUE;
	private static final Integer TIMEOUT = null;
	private final boolean continueAfterErrors;
	private final ExtractedModel<? extends AbstractModel> extractedModel;
	private final Map<String, String> prefs;
	private ExecuteModelCommand executeModelCommand;
	private State rootState;

	public ExecuteRun(final ExtractedModel<? extends AbstractModel> extractedModel, Map<String, String> prefs,
			boolean continueAfterErrors, ReusableAnimator animator) {
		this.extractedModel = extractedModel;
		this.continueAfterErrors = continueAfterErrors;
		this.prefs = prefs;
		this.animator = animator;
	}

	public void start() {
		this.executeModelCommand = null;
		this.rootState = null;

		final StateSpace oldStateSpace = this.animator.getCurrentStateSpace();
		if (oldStateSpace != null) {
			oldStateSpace.kill();
		}
		this.animator.resetProB();

		final Stopwatch loadStopwatch = Stopwatch.createStarted();
		final StateSpace stateSpace = this.animator.createStateSpace();
		stateSpace.changePreferences(this.prefs);
		this.extractedModel.loadIntoStateSpace(stateSpace);
		loadStopwatch.stop();
		LOGGER.info("Time to load model: {} ms", loadStopwatch.elapsed(TimeUnit.MILLISECONDS));

		final Stopwatch executeStopwatch = Stopwatch.createStarted();
		executeModel(stateSpace);
		executeStopwatch.stop();
		LOGGER.info("Time to run execute command: {} ms", executeStopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

	private void executeModel(final StateSpace stateSpace) {
		Trace t = new Trace(stateSpace);
		this.rootState = t.getCurrentState();
		executeModelCommand = new ExecuteModelCommand(stateSpace, t.getCurrentState(), MAX_NUMBER_OF_STATES_TO_BE_EXECUTED,
				continueAfterErrors, TIMEOUT);
		stateSpace.execute(executeModelCommand);
	}

	public ExecuteModelCommand getExecuteModelCommand() {
		return this.executeModelCommand;
	}

	public State getRootState() {
		return this.rootState;
	}

	public ReusableAnimator getUsedAnimator() {
		return this.animator;
	}

}
