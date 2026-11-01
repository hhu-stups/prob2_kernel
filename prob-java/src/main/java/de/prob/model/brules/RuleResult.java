package de.prob.model.brules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.hhu.stups.prob.translator.BNumber;
import de.hhu.stups.prob.translator.BSet;
import de.hhu.stups.prob.translator.BString;
import de.hhu.stups.prob.translator.BTuple;
import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EnumerationWarning;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.animator.domainobjects.TranslatedEvalResult;

public class RuleResult {
	private final RuleOperation ruleOperation;
	private final RuleStatus ruleStatus;
	private final int numberOfViolations, numberOfSuccesses, numberOfUnchecked;
	private final List<CounterExample> counterExamples = new ArrayList<>();
	private final List<SuccessMessage> successMessages = new ArrayList<>();
	private final List<SuccessMessage> uncheckedMessages = new ArrayList<>();

	// causes leading to NOT_CHECKED result
	private final ArrayList<String> allFailedDependencies = new ArrayList<>();
	private final ArrayList<String> allNotCheckedDependencies = new ArrayList<>();

	public RuleResult(RuleOperation rule, AbstractEvalResult result,
	                  AbstractEvalResult numberOfCounterExamples, AbstractEvalResult counterExampleResult,
	                  AbstractEvalResult numberOfSuccessMessages, AbstractEvalResult successMessageResult,
	                  AbstractEvalResult numberOfUncheckedMessages, AbstractEvalResult uncheckedMessageResult) {
		this.ruleOperation = rule;
		this.ruleStatus = RuleStatus.valueOf(result);

		this.numberOfViolations = parseNumberOfMessages(numberOfCounterExamples, "counter examples");
		if (this.numberOfViolations > 0 && counterExampleResult != null) {
			transformMessages(counterExampleResult, counterExamples, CounterExample::new);
			counterExamples.sort(Comparator.comparingInt(CounterExample::getErrorType)
				.thenComparing(CounterExample::getMessage));
		}

		this.numberOfSuccesses = parseNumberOfMessages(numberOfSuccessMessages, "success messages");
		if (this.numberOfSuccesses > 0 && successMessageResult != null) {
			transformMessages(successMessageResult, successMessages, SuccessMessage::new);
			successMessages.sort(Comparator.comparingInt(SuccessMessage::getRuleBodyCount).thenComparing(SuccessMessage::getMessage));
		}

		this.numberOfUnchecked = parseNumberOfMessages(numberOfUncheckedMessages, "unchecked messages");
		if (this.numberOfUnchecked > 0 && uncheckedMessageResult != null) {
			transformMessages(uncheckedMessageResult, uncheckedMessages, SuccessMessage::new);
			uncheckedMessages.sort(Comparator.comparingInt(SuccessMessage::getRuleBodyCount).thenComparing(SuccessMessage::getMessage));
		}
	}

	private static int parseNumberOfMessages(AbstractEvalResult numberOfMessages, String kind) {
		if (numberOfMessages instanceof EvalResult result) {
			try {
				if (Translator.translate(result.getValue()) instanceof BNumber value) {
					BigInteger number = value.bigIntegerValue();
					return number.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ? Integer.MAX_VALUE : number.intValueExact();
				}
			} catch (TranslationException e) {
				throw new IllegalStateException("expected an integer as number of " + kind + ", but was '" + result + "'", e);
			}
		} else if (numberOfMessages instanceof EnumerationWarning) {
			return -1;
		} else if (numberOfMessages instanceof IdentifierNotInitialised) {
			return 0;
		}
		throw new IllegalStateException("expected instance of EvalResult for enumerable " + kind +
			" or EnumerationWarning for an infinite number of " + kind + ", but was " +
			(numberOfMessages == null ? "null" : numberOfMessages.getClass() + " (" + numberOfMessages + ")"));
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

	public int getNumberOfUnchecked() {
		return this.numberOfUnchecked;
	}

	private static <T> void transformMessages(AbstractEvalResult abstractEvalResult, List<T> messages, BiFunction<Integer, String, T> messageConstructor) {
		if (!(abstractEvalResult instanceof EvalResult evalCurrent)) {
			// e.g. an evaluation error or a time-out while computing the messages
			messages.add(messageConstructor.apply(1, String.valueOf(abstractEvalResult)));
			return;
		}
		TranslatedEvalResult<BValue> translatedResult;
		try {
			translatedResult = evalCurrent.translate();
		} catch (Exception e) {
			/*- fall back solution if the result can not be parsed (e.g. {1,...,1000})
			 * should not happen because MAX_DISPLAY_SET is set to -1
			 * and hence, no truncated terms are delivered by ProBCore
			 * */
			final String message = evalCurrent.getValue().replace("\"", "");
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

	public List<SuccessMessage> getUncheckedMessages() {
		return this.uncheckedMessages;
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
		return !ruleOperation.getTags().isEmpty();
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
		if (this.numberOfUnchecked > 0) {
			sb.append(", NumberOfUnchecked: ").append(this.numberOfUnchecked);
			sb.append(", UncheckedMessages: ").append(this.uncheckedMessages);
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
