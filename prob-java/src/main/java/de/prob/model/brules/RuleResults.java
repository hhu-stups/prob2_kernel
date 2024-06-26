package de.prob.model.brules;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.State;

import java.util.*;

public class RuleResults {
	private final LinkedHashMap<String, RuleResult> ruleResultsMap = new LinkedHashMap<>();
	private final List<String> reqIds = new ArrayList<>();

	private ResultSummary summary;

	public RuleResults(RulesProject project, State state, int maxNumberOfReportedCounterExamples) {
		this(getRuleOperations(project), state, maxNumberOfReportedCounterExamples);
	}

	public RuleResults(RulesProject project, State state, int maxNumberOfReportedCounterExamples,
	                   int maxNumberOfSuccessMessages) {
		this(getRuleOperations(project), state, maxNumberOfReportedCounterExamples, maxNumberOfSuccessMessages);
	}

	private static Set<RuleOperation> getRuleOperations(RulesProject project) {
		final Set<RuleOperation> result = new HashSet<>();
		for (AbstractOperation operation : project.getOperationsMap().values()) {
			if (operation instanceof RuleOperation) {
				result.add((RuleOperation) operation);
			}
		}
		return result;
	}

	public RuleResults(Set<RuleOperation> ruleOperations, State state, int maxNumberOfReportedCounterExamples) {
		this(ruleOperations, state, maxNumberOfReportedCounterExamples, -1);
	}

	public RuleResults(Set<RuleOperation> ruleOperations, State state, int maxNumberOfReportedCounterExamples,
	                   int maxNumberOfSuccessMessages) {
		final ArrayList<RuleOperation> ruleList = new ArrayList<>();
		final List<IEvalElement> evalElements = new ArrayList<>();
		for (RuleOperation operation : ruleOperations) {
			ruleList.add(operation);
			evalElements.add(new ClassicalB(operation.getName()));

			// get number of counter examples
			// don't use FORCE here - enumeration warnings are handled by RuleResult
			String numberOfCtsFormula = String.format("card(%s)", operation.getCounterExampleVariableName());
			evalElements.add(new ClassicalB(numberOfCtsFormula));

			// get the (restricted) set of counter examples
			// FORCE should not be a problem here - we only use the result if card(%s) above was finite
			String ctFormula;
			if (maxNumberOfReportedCounterExamples == -1) {
				ctFormula = String.format("FORCE(ran(SORT(%s)))", operation.getCounterExampleVariableName());
			} else {
				ctFormula = String.format("FORCE(SORT(%s)[1..%s])", operation.getCounterExampleVariableName(),
					maxNumberOfReportedCounterExamples);
			}
			evalElements.add(new ClassicalB(ctFormula));

			// get number of success messages
			String numberOfSfFormula = String.format("card(%s)", operation.getSuccessfulVariableName());
			evalElements.add(new ClassicalB(numberOfSfFormula));

			// get the (restricted) set of success messages
			// FORCE should not be a problem here - we only use the result if card(%s) above was finite
			String sfFormula;
			if (maxNumberOfSuccessMessages == -1) {
				sfFormula = String.format("FORCE(ran(SORT(%s)))", operation.getSuccessfulVariableName());
			} else {
				sfFormula = String.format("FORCE(SORT(%s)[1..%s])", operation.getSuccessfulVariableName(),
					maxNumberOfSuccessMessages);
			}
			evalElements.add(new ClassicalB(sfFormula));
		}
		List<AbstractEvalResult> evalResults = state.eval(evalElements);
		for (int i = 0; i < ruleList.size(); i++) {
			int index = i * 5;
			RuleOperation ruleOperation = ruleList.get(i);
			RuleResult ruleResult = new RuleResult(ruleOperation, evalResults.get(index), evalResults.get(index + 1),
					evalResults.get(index + 2), evalResults.get(index + 3), evalResults.get(index + 4));
			ruleResultsMap.put(ruleOperation.getName(), ruleResult);

			if (ruleResult.hasRuleId()) {
				this.reqIds.add(ruleResult.getRuleId());
			}
		}
		addNotCheckedCauses();
	}

	private void addNotCheckedCauses() {
		final Set<String> allFailingRules = new HashSet<>();
		final Set<String> allNotCheckedRules = new HashSet<>();
		final Set<RuleResult> allNotCheckedRulesObjects = new HashSet<>();
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
				if (classificationMap.containsKey(classification)) {
					classificationMap.get(classification).add(ruleResult);
				} else {
					classificationMap.put(classification, new ArrayList<>(Collections.singleton(ruleResult)));
				}
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
