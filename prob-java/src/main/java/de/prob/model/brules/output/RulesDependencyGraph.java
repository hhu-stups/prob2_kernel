package de.prob.model.brules.output;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.FunctionOperation;
import de.prob.animator.domainobjects.DotCall;
import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.brules.ComputationStatus;
import de.prob.model.brules.OperationStatus;
import de.prob.model.brules.OperationStatuses;
import de.prob.model.brules.RuleStatus;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * used for partial dependency graph
 */
public class RulesDependencyGraph {

	public static IEvalElement getGraphExpression(final Trace currentTrace, final Collection<AbstractOperation> operations) {
		return currentTrace.getStateSpace().getModel().parseFormula(getGraphExpressionAsString(currentTrace, operations), FormulaExpand.EXPAND);
	}

	public static String getGraphExpressionAsString(final Trace currentTrace, final Collection<AbstractOperation> operations) {
		if (!(currentTrace.getModel() instanceof RulesModel rulesModel)) {
			throw new IllegalArgumentException("Expected Rules Model.");
		}

		// sorted by name to get a deterministic graph
		final Set<AbstractOperation> allOperations = new TreeSet<>(Comparator.comparing(AbstractOperation::getName));
		for (AbstractOperation operation : operations) {
			allOperations.add(operation);
			Set<AbstractOperation> dependencies = operation.getTransitiveDependencies();
			if (dependencies != null) {
				allOperations.addAll(dependencies);
			}
		}
		allOperations.removeIf(operation -> operation instanceof FunctionOperation);

		// Only evaluate the operations shown in the graph. Do not create a RulesChecker here:
		// this would analyse all dependencies of the project and modify the given trace (setExploreStateByDefault).
		final Map<AbstractOperation, OperationStatus> operationStates = OperationStatuses.getStatuses(
				rulesModel, new ArrayList<>(allOperations), currentTrace.getCurrentState());

		final List<String> nodes = new ArrayList<>();
		final List<String> edges = new ArrayList<>();
		for (AbstractOperation operation : allOperations) {
			final OperationStatus status = operationStates.get(operation);
			final String shape = status instanceof ComputationStatus ? "rectangle" : "ellipse";
			nodes.add("rec(shape: \"" + shape + "\", style: \"filled\", fillcolor: \"" + getFillColor(status)
					+ "\", nodes: \"" + operation.getName() + "\")");

			// highlight the reasons why an operation has not been checked/executed
			final boolean notChecked = status == RuleStatus.NOT_CHECKED || status == ComputationStatus.NOT_EXECUTED;
			for (AbstractOperation dependency : operation.getRequiredDependencies()) {
				final String edgeColor = notChecked ? getEdgeColor(operationStates.get(dependency)) : "black";
				edges.add("rec(color: \"" + edgeColor + "\", label: \"\", edge: \"" + operation.getName() + "\"|->\""
						+ dependency.getName() + "\")");
			}
		}

		return "rec(nodes: {" + String.join(",", nodes) + "}, edges: {" + String.join(",", edges) + "})";
	}

	private static String getFillColor(final OperationStatus status) {
		if (status instanceof RuleStatus ruleStatus) {
			return switch (ruleStatus) {
				case FAIL -> "#cc2f274d";
				case SUCCESS -> "#4caf504d";
				case NOT_CHECKED -> "transparent";
				case DISABLED -> "lightgray";
			};
		} else if (status instanceof ComputationStatus computationStatus) {
			return switch (computationStatus) {
				case EXECUTED -> "#4caf504d";
				case NOT_EXECUTED -> "transparent";
				case DISABLED -> "lightgray";
			};
		} else {
			return "transparent";
		}
	}

	private static String getEdgeColor(final OperationStatus dependencyStatus) {
		if (dependencyStatus instanceof RuleStatus ruleStatus) {
			return switch (ruleStatus) {
				case FAIL, DISABLED -> "#cc2f27";
				case SUCCESS -> "#4caf50";
				case NOT_CHECKED -> "black";
			};
		} else if (dependencyStatus == ComputationStatus.DISABLED) {
			return "#cc2f27";
		} else {
			return "black";
		}
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
