package de.prob.model.brules;

import com.google.common.base.Stopwatch;
import de.be4.classicalb.core.parser.rules.*;
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
import java.util.*;
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
	private final Map<AbstractOperation, Set<AbstractOperation>> successors = new HashMap<>();

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
				for (AbstractOperation abstractOperation : set) {
					if (!successors.containsKey(abstractOperation)) {
						successors.put(abstractOperation, new HashSet<>());
					}
					successors.get(abstractOperation).add(op);
				}
			}
		}
	}

	/**
	 * use if machine should be initialised before check
	 */
	public void init() {
		while (!trace.getCurrentState().isInitialised()) {
			trace = trace.anyOperation(null);
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
		while (true) {
			if (trace.getCurrentState().isExplored()) {
				// if the state has already been explored and we have a next transition: use this instead of computing a new one with execute_model
				// (should be faster)
				if (trace.getNextTransitions().isEmpty()) {
					break;
				}
				trace = trace.addTransitions(new ArrayList<>(trace.getNextTransitions()).subList(0,1));
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
			String transitionName = Optional.ofNullable(executeModelCommand.getSingleTransitionName()).orElse("");
			if (isInitTransition(transitionName)) {
				nrSteps--;
			}
			listener.progress(nrExecutedOperations+=nrSteps, transitionName);
		}
		stopwatch.stop();
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
		trace = trace.execute(op.getName());
		OperationStatus opState = getOperationState(op);
		stopwatch.stop();
		nrExecutedOperations++;
		return opState;
	}

	public Set<AbstractOperation> getExecutableOperations() {
		final Set<AbstractOperation> todo = new HashSet<>();
		Map<AbstractOperation, OperationStatus> operationStatuses = OperationStatuses.getStatuses(rulesModel, trace.getCurrentState());
		operationStatuses.forEach((op, status) -> {
			if (status.isNotExecuted() && !status.isDisabled()) {
				boolean canBeExecuted = true;
				// check that all dependencies are executed and have not failed in case of rules
				for (AbstractOperation pred : predecessors.get(op)) {
					OperationStatus predState = operationStatuses.get(pred);
					if (predState.isNotExecuted() || predState == RuleStatus.FAIL) {
						canBeExecuted = false;
						break;
					}
				}
				if (canBeExecuted) {
					todo.add(op);
				}
			}
		});
		return todo;
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

		for (AbstractOperation op : executionOrder) {
			if (!getExecutableOperations().contains(op)) {
				if (getOperationState(op).isExecuted()) { // required operation has already been executed
					if (listener != null) {
						listener.progress(nrExecutedOperations++, op.getName());
					}
					continue;
				}
				return false;
			}
			OperationStatus opState = executeOperation(op);
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
	 * @deprecated Use {@link #getOperationState(AbstractOperation)} instead.
	 */
	@Deprecated
	public OperationStatus evalOperation(State state, AbstractOperation operation) {
		return getOperationState(operation);
	}

	/**
	 * @deprecated Use {@link #getOperationStates()} instead.
	 */
	@Deprecated
	public Map<AbstractOperation, OperationStatus> evalOperations(State state, List<AbstractOperation> operations) {
		return getOperationStates();
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
		// checkThatOperationExists
		if (!rulesProject.getOperationsMap().containsKey(opName)) {
			throw new IllegalArgumentException("Unknown operation name: " + opName);
		}
		AbstractOperation op = rulesProject.getOperationsMap().get(opName);
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

	/**
	 * Use {@link #saveValidationReport(Path)} instead.
	 */
	@Deprecated
	public void saveValidationReport(final Path path, final Locale locale) {
		saveValidationReport(path);
	}
}
