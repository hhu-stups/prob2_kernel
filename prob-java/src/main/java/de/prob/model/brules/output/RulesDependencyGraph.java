package de.prob.model.brules.output;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.FunctionOperation;
import de.prob.animator.domainobjects.DotCall;
import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.brules.ComputationStatus;
import de.prob.model.brules.OperationStatus;
import de.prob.model.brules.RuleStatus;
import de.prob.model.brules.RulesChecker;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RulesDependencyGraph {

	public static IEvalElement getGraphExpression(final Trace currentTrace, final Collection<AbstractOperation> operations) {
		return currentTrace.getStateSpace().getModel().parseFormula(getGraphExpressionAsString(currentTrace, operations), FormulaExpand.EXPAND);
	}

	public static String getGraphExpressionAsString(final Trace currentTrace, final Collection<AbstractOperation> operations) {
		Set<AbstractOperation> allOperations = new HashSet<>(operations);
		for (AbstractOperation operation : operations) {
			allOperations.addAll(operation.getTransitiveDependencies());
		}
		allOperations.removeIf(operation -> operation instanceof FunctionOperation);
		RulesChecker rulesChecker = new RulesChecker(currentTrace);
		rulesChecker.init();
		Map<AbstractOperation, OperationStatus> operationStates = rulesChecker.getOperationStates();
		List<String> nodes = new ArrayList<>();
		List<String> edges = new ArrayList<>();
		for (AbstractOperation operation : allOperations) {
			String shape = "ellipse";
			String statusColor = "transparent";
			boolean notChecked = false;
			if (operationStates.get(operation) instanceof RuleStatus) {
				RuleStatus ruleStatus = (RuleStatus) operationStates.get(operation);
				switch (ruleStatus) {
					case FAIL:
						statusColor = "#cc2f274d";
						break;
					case SUCCESS:
						statusColor = "#4caf504d";
						break;
					case NOT_CHECKED:
						statusColor = "transparent";
						break;
					case DISABLED:
						statusColor = "lightgray";
						break;
					default:
						throw new IllegalArgumentException();
				}
				if (ruleStatus == RuleStatus.NOT_CHECKED) {
					notChecked = true;
				}
			} else if (operationStates.get(operation) instanceof ComputationStatus) {
				ComputationStatus computationStatus = (ComputationStatus) operationStates.get(operation);
				switch (computationStatus) {
					case EXECUTED:
						statusColor = "#4caf504d";
						break;
					case NOT_EXECUTED:
						statusColor = "transparent";
						break;
					case DISABLED:
						statusColor = "lightgray";
						break;
					default:
						throw new IllegalArgumentException();
				}
				if (computationStatus == ComputationStatus.NOT_EXECUTED) {
					notChecked = true;
				}
				shape = "rectangle";
			}
			nodes.add("rec(shape: \"" + shape + "\", style: \"filled\", fillcolor: \"" + statusColor + "\", nodes: \"" + operation.getName() + "\")");
			for (AbstractOperation dependency : operation.getRequiredDependencies()) {
				String edgeColor = "black";
				if (notChecked && operationStates.get(dependency) instanceof RuleStatus) {
					RuleStatus ruleStatus = (RuleStatus) operationStates.get(dependency);
					switch (ruleStatus) {
						case FAIL:
						case DISABLED:
							edgeColor = "#cc2f27";
							break;
						case SUCCESS:
							edgeColor = "#4caf50";
							break;
						case NOT_CHECKED:
							edgeColor = "black";
							break;
						default:
							throw new IllegalArgumentException();
					}
				} else if (notChecked && operationStates.get(dependency) instanceof ComputationStatus) {
					ComputationStatus computationStatus = (ComputationStatus) operationStates.get(dependency);
					if (Objects.requireNonNull(computationStatus) == ComputationStatus.DISABLED) {
						edgeColor = "#cc2f27";
					} else {
						edgeColor = "black";
					}
				}
				edges.add("rec(color: \"" + edgeColor + "\", label: \"\", edge: \"" + operation.getName() + "\"|->\"" + dependency.getName() + "\")");
			}
		}

		return "rec(nodes: {" + String.join(",", nodes) + "}, edges: {" + String.join(",", edges) + "})";
	}

	public static void saveGraph(final Trace trace, final Collection<AbstractOperation> operations,
	                             final Path path, final String dotOutputFormat) throws IOException, InterruptedException {

		byte[] dotContent = DotVisualizationCommand.getByName(DotVisualizationCommand.EXPRESSION_AS_GRAPH_NAME, trace)
			.visualizeAsDotToBytes(Collections.singletonList(getGraphExpression(trace, operations)));
		StateSpace stateSpace = trace.getStateSpace();

		Files.write(path, new DotCall(stateSpace.getCurrentPreference("DOT"))
			.layoutEngine(stateSpace.getCurrentPreference("DOT_ENGINE"))
			.outputFormat(dotOutputFormat)
			.input(dotContent)
			.call());
	}
}
