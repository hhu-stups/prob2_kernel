package de.prob.model.brules;

import com.google.common.base.Stopwatch;
import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.FunctionOperation;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.animator.command.ExecuteModelCommand;
import de.prob.animator.command.ExportRuleValidationReportCommand;
import de.prob.animator.domainobjects.DotCall;
import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.model.brules.output.RulesDependencyGraph;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class RulesChecker {
	public interface RulesCheckListener {
		void progress(int nrExecutedOperations, String opName);
	}

	private Trace trace;
	private final RulesModel rulesModel;
	private final RulesProject rulesProject;

	private final Stopwatch stopwatch;
	private int nrExecutedOperations = 0;

	private final Map<AbstractOperation, Set<AbstractOperation>> predecessors = new HashMap<>();

	public RulesChecker(Trace trace) {
		this.stopwatch = Stopwatch.createUnstarted();
		if (trace.getModel() instanceof RulesModel) {
			rulesModel = (RulesModel) trace.getModel();
			rulesProject = rulesModel.getRulesProject();
			determineDependencies();
		} else {
			throw new IllegalArgumentException("Expected Rules Model.");
		}
		setTrace(trace);
	}

	private void determineDependencies() {
		for (AbstractOperation op : rulesProject.getOperationsMap().values()) {
			if (!(op instanceof FunctionOperation)) {
				Set<AbstractOperation> set = op.getTransitiveDependencies().stream()
						.filter(p -> !(p instanceof FunctionOperation)).collect(Collectors.toSet());
				predecessors.put(op, set);
			}
		}
	}

	/**
	 * use if machine should be initialised before check
	 */
	public void init() {
		while (!trace.getCurrentState().isInitialised()) {
			Trace next = trace.anyOperation(null);
			if (next == trace) {
				// no enabled transition (e.g. no solution for the constants): avoid looping forever
				throw new IllegalStateException("Cannot initialise the rules machine: no transition is enabled in state "
						+ trace.getCurrentState().getId());
			}
			trace = next;
		}
		nrExecutedOperations = 0;
	}

	/**
	 * use direct 'execute' instead of 'animate' for faster execution of all rules.
	 */
	public void executeAllOperationsDirect(RulesCheckListener listener, int stepSize) {
		// TODO: consider using RulesMachineRun
		stopwatch.reset();
		stopwatch.start();
		try {
			while (true) {
				if (trace.getCurrentState().isExplored()) {
					// if the state has already been explored, and we have a next transition: use this instead of computing a new one with execute_model
					// (should be faster)
					List<Transition> outTransitions = trace.getCurrentState().getOutTransitions();
					if (outTransitions.isEmpty()) {
						break;
					}
					trace = trace.add(outTransitions.get(0));
					continue;
				}
				ExecuteModelCommand executeModelCommand = new ExecuteModelCommand(trace.getStateSpace(),
						trace.getCurrentState(), stepSize, true, true, null);
				trace.getStateSpace().execute(executeModelCommand);
				int nrSteps = executeModelCommand.getNumberOfStatesExecuted();
				if (nrSteps < 1) {
					break;
				}
				trace = trace.addTransitions(executeModelCommand.getNewTransitions());
				// addTransitions does not keep this setting; exploring every state again would be very slow
				trace.setExploreStateByDefault(false);
				String transitionName = Optional.ofNullable(executeModelCommand.getSingleTransitionName()).orElse("");
				if (isInitTransition(transitionName)) {
					nrSteps--;
				}
				nrExecutedOperations += nrSteps;
				if (listener != null) {
					listener.progress(nrExecutedOperations, transitionName);
				}
			}
		} finally {
			// also stop the stopwatch if the execution was interrupted or failed
			stop();
		}
	}

	private static boolean isInitTransition(String transition) {
		return transition.startsWith("$") && (transition.equals(Transition.SETUP_CONSTANTS_NAME) || transition.equals(Transition.INITIALISE_MACHINE_NAME));
	}

	public void executeAllOperations() {
		stopwatch.reset();
		init();
		// determine all operations that can be executed in this state
		Set<AbstractOperation> executableOperations = getExecutableOperations();
		while (!executableOperations.isEmpty()) {
			for (AbstractOperation op : executableOperations) {
				executeOperation(op);
			}
			executableOperations = getExecutableOperations();
		}
	}

	public OperationStatus executeOperation(AbstractOperation op) {
		stopwatch.start();
		final OperationStatus opState;
		try {
			trace = trace.execute(op.getName());
			opState = getOperationState(op);
		} finally {
			// otherwise the next call of start() would fail with an IllegalStateException
			stopwatch.stop();
		}
		nrExecutedOperations++;
		return opState;
	}

	public Set<AbstractOperation> getExecutableOperations() {
		final Set<AbstractOperation> todo = new HashSet<>();
		Map<AbstractOperation, OperationStatus> operationStatuses = OperationStatuses.getStatuses(rulesModel, trace.getCurrentState());
		operationStatuses.forEach((op, status) -> {
			if (canBeExecuted(op, operationStatuses)) {
				todo.add(op);
			}
		});
		return todo;
	}

	/**
	 * An operation can be executed if it has not been executed yet, is not disabled,
	 * and all its dependencies have been executed (and have not failed in case of rules).
	 *
	 * @param statuses must contain the status of the operation and of all its (transitive) dependencies
	 */
	private boolean canBeExecuted(AbstractOperation op, Map<AbstractOperation, OperationStatus> statuses) {
		OperationStatus status = statuses.get(op);
		if (status == null || !status.isNotExecuted() || status.isDisabled()) {
			return false;
		}
		for (AbstractOperation pred : predecessors.getOrDefault(op, Collections.emptySet())) {
			OperationStatus predState = statuses.get(pred);
			if (predState == null || predState.isNotExecuted() || predState == RuleStatus.FAIL) {
				return false;
			}
		}
		return true;
	}

	public boolean executeOperationAndDependencies(String opName) {
		return executeOperationAndDependencies(null, opName);
	}

	public boolean executeOperationAndDependencies(RulesCheckListener listener, String opName) {
		init();
		AbstractOperation goalOperation = getOperation(opName);
		List<AbstractOperation> executionOrder = goalOperation.getSortedListOfTransitiveDependencies();
		executionOrder.add(goalOperation);
		executionOrder.removeIf(FunctionOperation.class::isInstance);

		// Evaluate the status of all relevant operations only once and keep track of the changes locally
		// instead of re-evaluating the status of all operations of the project before every step.
		// This is sound because executing an operation only changes its own status
		// (DISABLED is already determined during the initialisation).
		final Map<AbstractOperation, OperationStatus> statuses = new HashMap<>(
				OperationStatuses.getStatuses(rulesModel, executionOrder, trace.getCurrentState()));
		for (AbstractOperation op : executionOrder) {
			if (!canBeExecuted(op, statuses)) {
				if (statuses.get(op).isExecuted()) { // required operation has already been executed
					if (listener != null) {
						listener.progress(++nrExecutedOperations, op.getName());
					}
					continue;
				}
				return false;
			}
			OperationStatus opState = executeOperation(op);
			statuses.put(op, opState);
			if (listener != null) {
				listener.progress(nrExecutedOperations, op.getName());
			}
			if (op != goalOperation && opState == RuleStatus.FAIL) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @deprecated Use {@link #getOperationState(AbstractOperation)} or
	 *     {@link OperationStatuses#getStatus(RulesModel, AbstractOperation, State)} instead.
	 */
	@Deprecated
	public OperationStatus evalOperation(State state, AbstractOperation operation) {
		// evaluate in the given state (and not in the current state of the trace)
		return OperationStatuses.getStatus(rulesModel, operation, state);
	}

	/**
	 * @deprecated Use {@link #getOperationStates()} or
	 *     {@link OperationStatuses#getStatuses(RulesModel, List, State)} instead.
	 */
	@Deprecated
	public Map<AbstractOperation, OperationStatus> evalOperations(State state, List<AbstractOperation> operations) {
		// evaluate the given operations in the given state (and not all operations in the current state of the trace)
		return OperationStatuses.getStatuses(rulesModel, operations, state);
	}

	public Trace getCurrentTrace() {
		return this.trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
		this.trace.setExploreStateByDefault(false);
		this.nrExecutedOperations = (int) trace.getElements().stream()
				.filter(e -> e.getTransition() != null && !isInitTransition(e.getTransition().getName()))
				.count();
	}

	public Map<AbstractOperation, OperationStatus> getOperationStates() {
		return OperationStatuses.getStatuses(rulesModel, trace.getCurrentState());
	}

	public OperationStatus getOperationState(AbstractOperation op) {
		return OperationStatuses.getStatus(rulesModel, op, trace.getCurrentState());
	}

	public OperationStatus getOperationState(String opName) {
		return getOperationState(getOperation(opName));
	}

	private AbstractOperation getOperation(String opName) {
		AbstractOperation op = rulesProject.getOperationsMap().get(opName);
		if (op == null) {
			throw new IllegalArgumentException("Unknown operation name: " + opName);
		}
		// checkThatOperationIsNotAFunctionOperation
		if (op instanceof FunctionOperation) {
			throw new IllegalArgumentException("Function operations are not supported: " + opName);
		}
		return op;
	}

	public void stop() {
		if (stopwatch.isRunning()) { // can happen after cancelling the execution in ProB2-UI
			stopwatch.stop();
		}
	}

	/**
	 * Saves complete dependency graph for all operations.
	 */
	public void saveDependencyGraph(final Path path, final String dotOutputFormat) throws IOException, InterruptedException {
		byte[] dotContent = DotVisualizationCommand.getByName(DotVisualizationCommand.RULE_DEPENDENCY_GRAPH_NAME, trace)
				.visualizeAsDotToBytes(new ArrayList<>());
		StateSpace stateSpace = trace.getStateSpace();

		Files.write(path, new DotCall(stateSpace.getCurrentPreference("DOT"))
				.layoutEngine(stateSpace.getCurrentPreference("DOT_ENGINE"))
				.outputFormat(dotOutputFormat)
				.input(dotContent)
				.call());
	}

	/**
	 * Saves partial dependency graph for provided operations.
	 */
	public void saveDependencyGraph(final Path path, final Collection<AbstractOperation> operations, final String dotOutputFormat)
			throws IOException, InterruptedException {
		RulesDependencyGraph.saveGraph(trace, operations, path, dotOutputFormat);
	}

	/**
	 * Save validation report with all rule results
	 *
	 * @param path if the file extension is .xml, report is saved as machine-readable XML, otherwise as interactive HTML
	 */
	public void saveValidationReport(final Path path) {
		trace.getStateSpace().execute(new ExportRuleValidationReportCommand(
				trace.getCurrentState().getId(), path.toFile(), stopwatch.elapsed().toMillis()));
	}

}
