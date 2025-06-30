package de.prob.model.brules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import de.be4.classicalb.core.parser.rules.*;
import de.hhu.stups.prob.translator.BNumber;
import de.hhu.stups.prob.translator.BSet;
import de.hhu.stups.prob.translator.BString;
import de.hhu.stups.prob.translator.BTuple;
import de.hhu.stups.prob.translator.BValue;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EnumerationWarning;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.TranslatedEvalResult;

public class RuleResult {
	private final RuleOperation ruleOperation;
	private final RuleStatus ruleStatus;
	private final int numberOfViolations, numberOfSuccesses;
	private final List<CounterExample> counterExamples = new ArrayList<>();
	private final List<SuccessMessage> successMessages = new ArrayList<>();

	// causes leading to NOT_CHECKED result
	private final ArrayList<String> allFailedDependencies = new ArrayList<>();
	private final ArrayList<String> allNotCheckedDependencies = new ArrayList<>();

	public RuleResult(RuleOperation rule, AbstractEvalResult result, AbstractEvalResult numberOfCounterExamples,
			AbstractEvalResult counterExampleResult, AbstractEvalResult numberOfSuccessMessages, AbstractEvalResult successMessageResult) {
		this.ruleOperation = rule;
		this.ruleStatus = RuleStatus.valueOf(result);
		if (numberOfCounterExamples instanceof EvalResult) {
			this.numberOfViolations = Integer.parseInt(((EvalResult) numberOfCounterExamples).getValue());
			transformCounterExamples(counterExampleResult);
		} else if (numberOfCounterExamples instanceof EnumerationWarning) {
			this.numberOfViolations = -1;
		} else {
			throw new IllegalStateException("expected instance of EvalResult for enumerable counter examples" +
				" or EnumerationWarning for an infinite number of counter examples, but was " + numberOfCounterExamples.getClass());
		}
		if (numberOfSuccessMessages instanceof EvalResult) {
			this.numberOfSuccesses = Integer.parseInt(((EvalResult) numberOfSuccessMessages).getValue());
			transformSuccessMessages(successMessageResult);
		} else if (numberOfSuccessMessages instanceof EnumerationWarning) {
			this.numberOfSuccesses = -1;
		} else {
			throw new IllegalStateException("expected instance of EvalResult for enumerable success messages" +
				" or EnumerationWarning for an infinite number of success messages, but was " + numberOfSuccessMessages.getClass());
		}
	}

	public RuleOperation getRuleOperation() {
		return this.ruleOperation;
	}

	public int getNumberOfViolations() {
		return this.numberOfViolations;
	}

	public int getNumberOfSuccesses() {
		return this.numberOfSuccesses;
	}

	private void transformCounterExamples(AbstractEvalResult abstractEvalResult) {
		transformMessages(abstractEvalResult, counterExamples, CounterExample::new);
		counterExamples.sort(Comparator.comparingInt(RuleResult.CounterExample::getErrorType)
			.thenComparing(RuleResult.CounterExample::getMessage));
	}

	private void transformSuccessMessages(AbstractEvalResult abstractEvalResult) {
		transformMessages(abstractEvalResult, successMessages, SuccessMessage::new);
		successMessages.sort(Comparator.comparingInt(RuleResult.SuccessMessage::getRuleBodyCount)
			.thenComparing(RuleResult.SuccessMessage::getMessage));
	}

	private static <T> void transformMessages(AbstractEvalResult abstractEvalResult, List<T> messages, BiFunction<Integer, String, T> messageConstructor) {
		EvalResult evalCurrent = (EvalResult) abstractEvalResult;
		TranslatedEvalResult<BValue> translatedResult;
		try {
			translatedResult = evalCurrent.translate();
		} catch (Exception e) {
			/*- fall back solution if the result can not be parsed (e.g. {1,...,1000})
			 * should not happen because MAX_DISPLAY_SET is set to -1
			 * and hence, no truncated terms are delivered by ProBCore
			 * */
			final String message = evalCurrent.getValue().replaceAll("\"", "");
			messages.add(messageConstructor.apply(1, message));
			return;
		}
		if (translatedResult.getValue() instanceof BSet<?>) {
			@SuppressWarnings("unchecked")
			BSet<BTuple<BNumber, BString>> set = (BSet<BTuple<BNumber, BString>>) translatedResult.getValue();
			set.stream()
					.map(tuple -> messageConstructor.apply(tuple.getFirst().intValue(), tuple.getSecond().stringValue()))
					.forEach(messages::add);
		} else {
			// fall back: should not happen
			messages.add(messageConstructor.apply(1, evalCurrent.getValue()));
		}
	}

	public void addAdditionalInformation(Set<String> allFailingRules, Set<String> allNotCheckedRules) {
		for (AbstractOperation abstractOperation : ruleOperation.getTransitiveDependencies()) {
			String operationName = abstractOperation.getName();
			if (allFailingRules.contains(operationName)) {
				this.allFailedDependencies.add(operationName);
			} else if (allNotCheckedRules.contains(operationName)) {
				allNotCheckedDependencies.add(operationName);
			}
		}
	}

	public List<String> getFailedDependencies() {
		return this.allFailedDependencies;
	}

	public List<String> getNotCheckedDependencies() {
		return this.allNotCheckedDependencies;
	}

	public List<CounterExample> getCounterExamples() {
		return this.counterExamples;
	}

	public List<SuccessMessage> getSuccessMessages() {
		return this.successMessages;
	}

	public String getRuleName() {
		return this.ruleOperation.getName();
	}

	public boolean hasRuleId() {
		return ruleOperation.getRuleIdString() != null;
	}

	public String getRuleId() {
		return ruleOperation.getRuleIdString();
	}

	public boolean hasClassification() {
		return ruleOperation.getClassification() != null;
	}

	public String getClassification() {
		return ruleOperation.getClassification();
	}

	public boolean hasTags() {
		return ruleOperation.getTags().isEmpty();
	}

	public List<String> getTags() {
		return ruleOperation.getTags();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[OperationName: ").append(this.getRuleName());
		sb.append(", Result: ").append(this.getRuleState());
		if (this.getRuleId() != null) {
			sb.append(", RuleID: ").append(this.getRuleId());
		}
		if (this.numberOfViolations > 0) {
			sb.append(", NumberOfViolations: ").append(this.numberOfViolations);
			sb.append(", Violations: ").append(this.counterExamples);
		}
		if (this.numberOfSuccesses > 0) {
			sb.append(", NumberOfSuccesses: ").append(this.numberOfSuccesses);
			sb.append(", SuccessMessages: ").append(this.successMessages);
		}
		if (!this.allFailedDependencies.isEmpty()) {
			sb.append(", FailedDependencies: ").append(this.allFailedDependencies);
		}
		if (!this.allNotCheckedDependencies.isEmpty()) {
			sb.append(", NotCheckedDependencies: ").append(this.allNotCheckedDependencies);
		}
		sb.append("]");
		return sb.toString();
	}

	public RuleStatus getRuleState() {
		return this.ruleStatus;
	}

	public boolean hasFailed() {
		return this.ruleStatus == RuleStatus.FAIL;
	}

	public static class CounterExample {
		private final int errorType;
		private final String message;

		public CounterExample(int errorType, String message) {
			this.errorType = errorType;
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}

		public int getErrorType() {
			return this.errorType;
		}

		@Override
		public String toString() {
			return this.message;
		}
	}

	public static class SuccessMessage {
		private final int ruleBodyCount;
		private final String message;

		public SuccessMessage(int ruleBodyCount, String message) {
			this.ruleBodyCount = ruleBodyCount;
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}

		public int getRuleBodyCount() {
			return this.ruleBodyCount;
		}

		@Override
		public String toString() {
			return this.message;
		}
	}
}
