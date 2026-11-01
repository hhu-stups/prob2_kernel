package de.prob.model.brules;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.rules.RulesTransformation;
import de.prob.animator.domainobjects.AbstractEvalResult;

public enum ComputationStatus implements OperationStatus {

	EXECUTED(RulesTransformation.COMPUTATION_EXECUTED),
	DISABLED(RulesTransformation.COMPUTATION_DISABLED),
	NOT_EXECUTED(RulesTransformation.COMPUTATION_NOT_EXECUTED);

	private final String bValue;

	private static final Map<String, ComputationStatus> mapping = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(status -> status.bValue, Function.identity()));

	ComputationStatus(String value) {
		this.bValue = value;
	}

	public static ComputationStatus valueOf(AbstractEvalResult evalResult) {
		return OperationStatus.fromEvalResult(evalResult, mapping, NOT_EXECUTED);
	}

	@Override
	public boolean isExecuted() {
		return this == EXECUTED;
	}

	@Override
	public boolean isNotExecuted() {
		return this == NOT_EXECUTED;
	}

	@Override
	public boolean isDisabled() {
		return this == DISABLED;
	}

}
