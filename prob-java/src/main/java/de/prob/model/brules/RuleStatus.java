package de.prob.model.brules;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.rules.RulesTransformation;
import de.prob.animator.domainobjects.AbstractEvalResult;

public enum RuleStatus implements OperationStatus {

	FAIL(RulesTransformation.RULE_FAIL),
	SUCCESS(RulesTransformation.RULE_SUCCESS),
	NOT_CHECKED(RulesTransformation.RULE_NOT_CHECKED),
	DISABLED(RulesTransformation.RULE_DISABLED);

	private final String bValue;

	RuleStatus(String bValue) {
		this.bValue = bValue;
	}

	private static final Map<String, RuleStatus> mapping = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(status -> status.bValue, Function.identity()));

	public static RuleStatus valueOf(AbstractEvalResult evalResult) {
		return OperationStatus.fromEvalResult(evalResult, mapping, NOT_CHECKED);
	}

	@Override
	public boolean isExecuted() {
		return this == FAIL || this == SUCCESS;
	}

	@Override
	public boolean isNotExecuted() {
		return this == NOT_CHECKED || this == DISABLED;
	}

	@Override
	public boolean isDisabled() {
		return this == RuleStatus.DISABLED;
	}

}
