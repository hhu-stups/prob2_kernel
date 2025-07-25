package de.prob.model.brules;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.GetTotalNumberOfErrorsCommand;
import de.prob.animator.domainobjects.StateError;
import de.prob.exception.ProBError;
import de.prob.statespace.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesMachineRun {

	public enum ERROR_TYPES {
		PARSE_ERROR, PROB_ERROR, UNEXPECTED_ERROR
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesMachineRun.class);

	private final RulesMachineRunner rulesMachineRunner;

	private RulesProject rulesProject;
	private ExecuteRun executeRun;

	private final List<Error> errors;

	private final File runnerFile;
	private final Map<String, String> proBCorePreferences;
	private final Map<String, String> constantValuesToBeInjected;

	private RuleResults ruleResults;
	private int maxNumberOfReportedCounterExamples = 50;
	private int maxNumberOfReportedSuccessMessages = 50;
	private int maxNumberOfReportedUncheckedMessages = 50;

	private BigInteger totalNumberOfProBCliErrors;

	private boolean continueAfterErrors = false;

	private ReusableAnimator animator;

	public RulesMachineRun(RulesMachineRunner rulesMachineRunner, File runnerFile) {
		this(rulesMachineRunner, runnerFile, new HashMap<>(), new HashMap<>());
	}

	public RulesMachineRun(RulesMachineRunner rulesMachineRunner, File runnerFile, Map<String, String> prefs, Map<String, String> constantValuesToBeInjected) {
		this.rulesMachineRunner = rulesMachineRunner;
		this.runnerFile = runnerFile;
		this.errors = new ArrayList<>();
		this.proBCorePreferences = new HashMap<>();
		if (prefs != null) {
			this.proBCorePreferences.putAll(prefs);
		}
		// add mandatory preferences
		this.proBCorePreferences.put("TRY_FIND_ABORT", "TRUE");
		this.proBCorePreferences.put("CLPFD", "FALSE");
		this.proBCorePreferences.put("MAX_DISPLAY_SET", "-1");
		this.proBCorePreferences.put("ENUMERATE_INFINITE_TYPES", "FALSE");
		// maybe add DATA_VALIDATION TRUE

		this.constantValuesToBeInjected = constantValuesToBeInjected;
	}

	public void setMaxNumberOfReportedCounterExamples(int i) {
		this.maxNumberOfReportedCounterExamples = i;
	}

	public void setMaxNumberOfReportedSuccessMessages(int i) {
		this.maxNumberOfReportedSuccessMessages = i;
	}

	public void setMaxNumberOfReportedUncheckedMessages(int i) {
		this.maxNumberOfReportedUncheckedMessages = i;
	}

	public void setContinueAfterErrors(boolean continueAfterErrors) {
		this.continueAfterErrors = continueAfterErrors;
	}

	public void start() {
		LOGGER.info("Starting rules machine run: {}", this.runnerFile.getAbsolutePath());
		final Stopwatch parsingStopwatch = Stopwatch.createStarted();
		boolean hasParseErrors = parseAndTranslateRulesProject();
		parsingStopwatch.stop();
		LOGGER.info("Time to parse rules project: {} ms", parsingStopwatch.elapsed(TimeUnit.MILLISECONDS));
		if (hasParseErrors) {
			LOGGER.error("RULES_MACHINE has errors!");
			return;
		}
		this.executeRun = rulesMachineRunner.createRulesMachineExecuteRun(this.rulesProject, runnerFile,
				this.proBCorePreferences, continueAfterErrors, this.getAnimator());
		try {
			LOGGER.info("Start execute ...");
			final Stopwatch executeStopwatch = Stopwatch.createStarted();
			this.executeRun.start();
			executeStopwatch.stop();
			LOGGER.info("Execute run finished. Time: {} ms", executeStopwatch.elapsed(TimeUnit.MILLISECONDS));
		} catch (ProBError e) {
			LOGGER.error("ProBError: {}", e.getMessage());
			this.errors.add(new Error(ERROR_TYPES.PROB_ERROR, e.getMessage(), e));
			if (executeRun.getExecuteModelCommand() != null) {
				try {
					State finalState = executeRun.getExecuteModelCommand().getFinalState();
					// explores the final state and can throw a ProBError
					Collection<StateError> stateErrors = finalState.getStateErrors();
					for (StateError stateError : stateErrors) {
						this.errors.add(new Error(ERROR_TYPES.PROB_ERROR, stateError.getLongDescription(), e));
					}
				} catch (ProBError e2) {
					// Enumeration errors
					this.errors.add(new Error(ERROR_TYPES.PROB_ERROR, e2.getMessage(), e2));
					return;
				}
			} else {
				/*- static errors such as type errors or errors while loading the  state space */
				this.errors.add(new Error(ERROR_TYPES.PROB_ERROR, e.getMessage(), e));
				/*- no final state is available and thus we can not create RuleResults */
				return;
			}
		} catch (Exception e) {
			LOGGER.error("Unexpected error occurred: {}", e.getMessage(), e);
			// storing all error messages
			this.errors.add(new Error(ERROR_TYPES.PROB_ERROR, e.getMessage(), e));
			return;
		} finally {
			GetTotalNumberOfErrorsCommand totalNumberOfErrorsCommand = new GetTotalNumberOfErrorsCommand();
			executeRun.getUsedAnimator().execute(totalNumberOfErrorsCommand);
			totalNumberOfProBCliErrors = totalNumberOfErrorsCommand.getTotalNumberOfErrors();
		}

		this.animator = this.executeRun.getUsedAnimator();
		final Stopwatch extractResultsStopwatch = Stopwatch.createStarted();
		this.ruleResults = new RuleResults(this.rulesProject, executeRun.getExecuteModelCommand().getFinalState(),
				maxNumberOfReportedCounterExamples, maxNumberOfReportedSuccessMessages,
				maxNumberOfReportedUncheckedMessages);
		extractResultsStopwatch.stop();
		LOGGER.info("Time to extract results from final state: {}", extractResultsStopwatch.elapsed(TimeUnit.MILLISECONDS));

	}

	private boolean parseAndTranslateRulesProject() {
		this.rulesProject = new RulesProject();
		ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setAddLineNumbers(true);
		rulesProject.setParsingBehaviour(parsingBehaviour);
		rulesProject.parseProject(runnerFile);

		for (Entry<String, String> pair : constantValuesToBeInjected.entrySet()) {
			rulesProject.addConstantValue(pair.getKey(), pair.getValue());
		}

		/*
		 * parse errors and errors from semantic checks are stored in the
		 * rulesProject
		 */
		rulesProject.checkAndTranslateProject();
		if (rulesProject.hasErrors()) {
			BException bException = rulesProject.getBExceptionList().get(0);
			String message = bException.getMessage();
			LOGGER.error("Parse error:  {}", message);

			this.errors.add(new Error(ERROR_TYPES.PARSE_ERROR, message, bException));
		}
		return rulesProject.hasErrors();
	}

	public boolean hasError() {
		return !this.errors.isEmpty();
	}

	public List<Error> getErrorList() {
		return new ArrayList<>(this.errors);
	}

	/**
	 * 
	 * @return the first error found or {@code null} if no error has occurred
	 */
	public Error getFirstError() {
		if (this.errors.isEmpty()) {
			return null;
		} else {
			return this.errors.get(0);
		}

	}

	public RulesProject getRulesProject() {
		return this.rulesProject;
	}

	public RuleResults getRuleResults() {
		return this.ruleResults;
	}

	public ExecuteRun getExecuteRun() {
		return this.executeRun;
	}

	public File getRunnerFile() {
		return runnerFile;
	}

	/**
	 * Returns the total number of errors recorded by a concrete ProB cli
	 * instance. Note, if the ProB cli instance is reused for further
	 * RulesMachineRuns, this number is NOT reset. Can be {@code null} if there
	 * is no state space available. Moreover, this number does not match the
	 * size of the {@link RulesMachineRun#errors} list.
	 * 
	 * @return total number of ProB cli errors
	 */
	public BigInteger getTotalNumberOfProBCliErrors() {
		return this.totalNumberOfProBCliErrors;
	}

	public ReusableAnimator getAnimator() {
		return this.animator;
	}

	public void setAnimator(final ReusableAnimator animator) {
		this.animator = animator;
	}

	public static class Error {
		final ERROR_TYPES type;
		final String message;
		final Exception exception;

		public ERROR_TYPES getType() {
			return this.type;
		}

		public String getMessage() {
			return this.message;
		}

		public Exception getException() {
			return this.exception;
		}

		@Override
		public String toString() {
			return type + ": " + message;
		}

		Error(ERROR_TYPES type, String message, Exception exception) {
			this.type = type;
			this.message = message;
			this.exception = exception;
		}
	}

}
