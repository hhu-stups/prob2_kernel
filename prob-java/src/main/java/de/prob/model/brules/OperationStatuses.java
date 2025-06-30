package de.prob.model.brules;

import de.be4.classicalb.core.parser.rules.*;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.statespace.State;

import java.util.*;
import java.util.stream.Collectors;

public class OperationStatuses {

	private final Map<AbstractOperation, OperationStatus> statuses = new HashMap<>();

	private OperationStatuses(RulesModel model, State state) {
		this(model, new ArrayList<>(model.getRulesProject().getOperationsMap().values()), state);
	}

	private OperationStatuses(RulesModel model, List<AbstractOperation> operations, State state) {
		final List<AbstractOperation> filteredOps = new ArrayList<>(operations);
		filteredOps.removeIf(FunctionOperation.class::isInstance);

		List<AbstractEvalResult> evalResults = state.eval(
				filteredOps.stream().map(model::getEvalElement).collect(Collectors.toList()));
		for (int i = 0; i < filteredOps.size(); i++) {
			AbstractOperation op = filteredOps.get(i);
			if (op instanceof RuleOperation) {
				this.statuses.put(op, RuleStatus.valueOf(evalResults.get(i)));
			} else if (op instanceof ComputationOperation) {
				this.statuses.put(op, ComputationStatus.valueOf(evalResults.get(i)));
			}
		}
	}

	public static Map<AbstractOperation, OperationStatus> getStatuses(RulesModel model, State state) {
		return new OperationStatuses(model, state).statuses;
	}

	public static Map<AbstractOperation, OperationStatus> getStatuses(RulesModel model, List<AbstractOperation> operations, State state) {
		return new OperationStatuses(model, operations, state).statuses;
	}

	public static OperationStatus getStatus(RulesModel model, AbstractOperation comp, State state) {
		return new OperationStatuses(model, Collections.singletonList(comp), state).statuses.get(comp);
	}

	@Override
	public String toString() {
		return statuses.toString();
	}

}
