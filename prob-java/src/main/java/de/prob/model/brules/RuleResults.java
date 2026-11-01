package de.prob.model.brules;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.hhu.stups.prob.translator.BNumber;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.State;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuleResults {

	private static final int FORMULAS_PER_RULE = 4;

	private final Map<String, RuleResult> ruleResultsMap = new LinkedHashMap<>();

	private ResultSummary summary;

	public RuleResults(RulesProject project, State state, int maxNumberOfReportedCounterExamples) {
		this(getRuleOperations(project), state, maxNumberOfReportedCounterExamples);
	}

	public RuleResults(RulesProject project, State state, int maxNumberOfReportedCounterExamples,
	                   int maxNumberOfSuccessMessages) {
		this(getRuleOperations(project), state, maxNumberOfReportedCounterExamples, maxNumberOfSuccessMessages);
	}

	public RuleResults(RulesProject project, State state, int maxNumberOfReportedCounterExamples,
	                   int maxNumberOfSuccessMessages, int maxNumberOfUncheckedMessages) {
		this(getRuleOperations(project), state, maxNumberOfReportedCounterExamples, maxNumberOfSuccessMessages, maxNumberOfUncheckedMessages);
	}

	private static Set<RuleOperation> getRuleOperations(RulesProject project) {
		// keep the order of the project to get deterministic results
		final Set<RuleOperation> result = new LinkedHashSet<>();
		for (AbstractOperation operation : project.getOperationsMap().values()) {
			if (operation instanceof RuleOperation) {
				result.add((RuleOperation) operation);
			}
		}
		return result;
	}

	public RuleResults(Set<RuleOperation> ruleOperations, State state, int maxNumberOfReportedCounterExamples) {
		this(ruleOperations, state, maxNumberOfReportedCounterExamples, -1, -1);
	}

	public RuleResults(Set<RuleOperation> ruleOperations, State state, int maxNumberOfReportedCounterExamples, int  maxNumberOfSuccessMessages) {
		this(ruleOperations, state, maxNumberOfReportedCounterExamples, maxNumberOfSuccessMessages, -1);
	}

	/**
	 * Extracts the results of the given rules from the given state.
	 * A negative maximum number of messages means that all messages are reported.
	 */
	public RuleResults(Set<RuleOperation> ruleOperations, State state, int maxNumberOfReportedCounterExamples,
	                   int maxNumberOfSuccessMessages, int maxNumberOfUncheckedMessages) {
		final List<RuleOperation> ruleList = new ArrayList<>(ruleOperations);

		// 1st round: status of each rule and number of messages
		// don't use FORCE here - enumeration warnings are handled by RuleResult
		final List<IEvalElement> evalElements = new ArrayList<>(ruleList.size() * FORMULAS_PER_RULE);
		for (RuleOperation operation : ruleList) {
			evalElements.add(new ClassicalB(operation.getName()));
			evalElements.add(new ClassicalB(String.format("card(%s)", operation.getCounterExampleVariableName())));
			evalElements.add(new ClassicalB(String.format("card(%s)", operation.getSuccessfulVariableName())));
			evalElements.add(new ClassicalB(String.format("card(%s)", operation.getUncheckedVariableName())));
		}
		final List<AbstractEvalResult> evalResults = state.eval(evalElements);

		// 2nd round: only fetch the messages of non-empty, finite message sets.
		// This avoids FORCE-ing infinite sets and needless evaluation of empty ones.
		final List<IEvalElement> messageElements = new ArrayList<>();
		final Map<Integer, Integer> messageIndex = new HashMap<>();
		for (int i = 0; i < ruleList.size(); i++) {
			RuleOperation operation = ruleList.get(i);
			int base = i * FORMULAS_PER_RULE;
			addMessageFormula(messageElements, messageIndex, base + 1, evalResults.get(base + 1),
				operation.getCounterExampleVariableName(), maxNumberOfReportedCounterExamples);
			addMessageFormula(messageElements, messageIndex, base + 2, evalResults.get(base + 2),
				operation.getSuccessfulVariableName(), maxNumberOfSuccessMessages);
			addMessageFormula(messageElements, messageIndex, base + 3, evalResults.get(base + 3),
				operation.getUncheckedVariableName(), maxNumberOfUncheckedMessages);
		}
		final List<AbstractEvalResult> messageResults = messageElements.isEmpty()
			? Collections.emptyList()
			: state.eval(messageElements);

		for (int i = 0; i < ruleList.size(); i++) {
			int base = i * FORMULAS_PER_RULE;
			RuleOperation ruleOperation = ruleList.get(i);
			RuleResult ruleResult = new RuleResult(ruleOperation, evalResults.get(base),
					evalResults.get(base + 1), getMessageResult(messageResults, messageIndex, base + 1),
					evalResults.get(base + 2), getMessageResult(messageResults, messageIndex, base + 2),
					evalResults.get(base + 3), getMessageResult(messageResults, messageIndex, base + 3));
			ruleResultsMap.put(ruleOperation.getName(), ruleResult);
		}
		addNotCheckedCauses();
	}

	private static void addMessageFormula(List<IEvalElement> messageElements, Map<Integer, Integer> messageIndex,
	                                      int countIndex, AbstractEvalResult countResult, String variableName, int limit) {
		if (limit == 0 || !(countResult instanceof EvalResult)) {
			// no messages requested, not initialised or not enumerable (infinite)
			return;
		}
		BigInteger count = BigInteger.ZERO;
		try {
			if (Translator.translate(((EvalResult) countResult).getValue()) instanceof BNumber value) {
				count = value.bigIntegerValue();
			}
		} catch (TranslationException e) {
			return;
		}
		if (count.signum() <= 0) {
			return;
		}
		final String formula;
		if (limit < 0 || count.compareTo(BigInteger.valueOf(limit)) <= 0) {
			// all messages are reported - no need to sort them in ProB, they are sorted by RuleResult
			formula = String.format("FORCE(%s)", variableName);
		} else {
			formula = String.format("FORCE(SORT(%s)[1..%s])", variableName, limit);
		}
		messageIndex.put(countIndex, messageElements.size());
		messageElements.add(new ClassicalB(formula));
	}

	private static AbstractEvalResult getMessageResult(List<AbstractEvalResult> messageResults,
	                                                   Map<Integer, Integer> messageIndex, int countIndex) {
		Integer index = messageIndex.get(countIndex);
		return index == null ? null : messageResults.get(index);
	}

	private void addNotCheckedCauses() {
		final Set<String> allFailingRules = new HashSet<>();
		final Set<String> allNotCheckedRules = new HashSet<>();
		final List<RuleResult> allNotCheckedRulesObjects = new ArrayList<>();
		for (RuleResult ruleResult : ruleResultsMap.values()) {
			RuleStatus result = ruleResult.getRuleState();
			if (result == RuleStatus.FAIL) {
				allFailingRules.add(ruleResult.getRuleName());
			} else if (result == RuleStatus.NOT_CHECKED) {
				allNotCheckedRules.add(ruleResult.getRuleName());
				allNotCheckedRulesObjects.add(ruleResult);
			}
		}
		for (RuleResult ruleResult : allNotCheckedRulesObjects) {
			ruleResult.addAdditionalInformation(allFailingRules, allNotCheckedRules);
		}
	}

	private void createSummary() {
		final int numberOfRules = ruleResultsMap.size();
		int numberOfRulesFailed = 0;
		int numberOfRulesSucceeded = 0;
		int numberOfRulesNotChecked = 0;
		int numberOfRulesDisabled = 0;
		for (RuleResult ruleResult : ruleResultsMap.values()) {
			RuleStatus resultEnum = ruleResult.getRuleState();
			switch (resultEnum) {
			case FAIL:
				numberOfRulesFailed++;
				break;
			case SUCCESS:
				numberOfRulesSucceeded++;
				break;
			case NOT_CHECKED:
				numberOfRulesNotChecked++;
				break;
			case DISABLED:
				numberOfRulesDisabled++;
				break;
			default:
				throw new AssertionError();
			}
		}
		this.summary = new ResultSummary(numberOfRules, numberOfRulesFailed, numberOfRulesSucceeded,
			numberOfRulesNotChecked, numberOfRulesDisabled);
	}

	public List<RuleResult> getRuleResultList() {
		return new ArrayList<>(ruleResultsMap.values());
	}

	public Map<String, RuleResult> getRuleResultMap() {
		return new HashMap<>(this.ruleResultsMap);
	}

	public Map<String, List<RuleResult>> getRuleResultsForClassifications() {
		Map<String, List<RuleResult>> classificationMap = new HashMap<>();
		for (RuleResult ruleResult : this.ruleResultsMap.values()) {
			if (ruleResult.hasClassification()) {
				String classification = ruleResult.getClassification();
				classificationMap.computeIfAbsent(classification, k -> new ArrayList<>()).add(ruleResult);
			}
		}
		return classificationMap;
	}

	public List<RuleResult> getRuleResultsWithoutClassification() {
		List<RuleResult> ruleResults = new ArrayList<>();
		for (RuleResult ruleResult : this.ruleResultsMap.values()) {
			if (!ruleResult.hasClassification()) {
				ruleResults.add(ruleResult);
			}
		}
		return ruleResults;
	}

	public ResultSummary getSummary() {
		if (this.summary == null) {
			createSummary();
		}
		return this.summary;
	}

	public RuleResult getRuleResult(final String ruleName) {
		return this.ruleResultsMap.get(ruleName);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (RuleResult result : this.ruleResultsMap.values()) {
			sb.append(result.toString()).append("\n");
		}
		return sb.toString();
	}

	public static class ResultSummary {
		public final int numberOfRules;
		public final int numberOfRulesFailed;
		public final int numberOfRulesSucceeded;
		public final int numberOfRulesNotChecked;
		public final int numberOfRulesDisabled;
		protected ResultSummary(int numberOfRules, int numberOfRulesFailed, int numberOfRulesSucceeded,
				int numberOfRulesNotChecked, int numberOfRulesDisabled) {
			this.numberOfRules = numberOfRules;
			this.numberOfRulesFailed = numberOfRulesFailed;
			this.numberOfRulesSucceeded = numberOfRulesSucceeded;
			this.numberOfRulesNotChecked = numberOfRulesNotChecked;
			this.numberOfRulesDisabled = numberOfRulesDisabled;
		}

		@Override
		public String toString() {
			return "[Rules: " + numberOfRules +
				", failed rules: " + numberOfRulesFailed +
				", succeeded rules: " + numberOfRulesSucceeded +
				", not checked rules: " + numberOfRulesNotChecked +
				", disabled rules: " + numberOfRulesDisabled + "]";
		}
	}
}
