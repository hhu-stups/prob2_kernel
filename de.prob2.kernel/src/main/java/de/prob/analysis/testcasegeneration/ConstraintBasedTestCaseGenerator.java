package de.prob.analysis.testcasegeneration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.analysis.FeasibilityAnalysis;
import de.prob.analysis.mcdc.ConcreteMCDCTestCase;
import de.prob.analysis.mcdc.MCDCIdentifier;
import de.prob.analysis.testcasegeneration.testtrace.CoverageTestTrace;
import de.prob.analysis.testcasegeneration.testtrace.MCDCTestTrace;
import de.prob.analysis.testcasegeneration.testtrace.TestTrace;
import de.prob.animator.command.ConstraintBasedSequenceCheckCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.Join;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.Extraction;
import de.prob.model.representation.Machine;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

/**
 * Performs constraint-based test case generation.
 * <p>
 * The generator can be executed with different coverage objectives. Currently available coverage options:
 * - Operation Coverage
 * - MC/DC Coverage
 */
public class ConstraintBasedTestCaseGenerator {

	private final StateSpace stateSpace;
	private final TestCaseGeneratorSettings settings;
	private final List<String> finalOperations;
	private List<String> infeasibleOperations;
	private List<Target> targets;
	private List<Target> uncoveredTargets = new ArrayList<>();

	public ConstraintBasedTestCaseGenerator(StateSpace stateSpace, TestCaseGeneratorSettings settings, List<String> finalOperations) {
		this.stateSpace = stateSpace;
		this.settings = settings;
		this.finalOperations = finalOperations;
	}
	
	/**
	 * Performs the test case generation.
	 *
	 * @return A {@link TestCaseGeneratorResult} containing the final test cases and the targets left uncovered.
	 */
	public TestCaseGeneratorResult generateTestCases() {
		boolean interrupted = false;
		List<TestTrace> testTraces = new ArrayList<>();
		int maxDepth = settings.getMaxDepth();

		if(settings instanceof TestCaseGeneratorMCDCSettings) {
			targets = getMCDCTargets(((TestCaseGeneratorMCDCSettings) settings).getLevel());
			testTraces.add(new MCDCTestTrace(new ArrayList<>(), null, new ArrayList<>(), false));
		} else if(settings instanceof TestCaseGeneratorOperationCoverageSettings) {
			List<String> selectedOperations = new ArrayList<>(((TestCaseGeneratorOperationCoverageSettings) settings).getOperations());
			targets = getOperationCoverageTargets(selectedOperations);
			testTraces.add(new CoverageTestTrace(new ArrayList<>(), null, false));
		} else {
			return new TestCaseGeneratorResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), interrupted);
		}

		infeasibleOperations = new FeasibilityAnalysis(stateSpace).analyseFeasibility();
		discardInfeasibleTargets();

		int depth = 0;
		List<Target> tempTargets;
		Set<Target> visitedTargets = new HashSet<>();
		while (true) {
			tempTargets = new ArrayList<>(targets);
			List<TestTrace> tracesOfCurrentDepth = filterDepthAndFinal(testTraces, depth);
			for (TestTrace trace : tracesOfCurrentDepth) {
				for (Target t : new ArrayList<>(targets)) {
					ConstraintBasedSequenceCheckCommand cmd = findTestPath(trace, t);
					if (cmd.getResult() != ConstraintBasedSequenceCheckCommand.ResultType.NO_PATH_FOUND && !visitedTargets.contains(t)) {
						targets.remove(t);
						Trace previousTrace = cmd.getTrace();
						cmd = findTestPathWithTarget(trace, t);
						Trace currentTrace = cmd.getTrace();
						TestTrace newTrace = trace.createNewTrace(trace.getTransitionNames(), t, // a trace is complete, i.e., should not be extended further if it contains a final operation or is statically proven to be infeasible
								(finalOperations.contains(t.getOperation()) || t.isInfeasible()), t.isInfeasible() ? previousTrace : currentTrace);
						testTraces.add(newTrace);
						visitedTargets.add(t);
					}
				}
				if(Thread.currentThread().isInterrupted()) {
					interrupted = true;
					break;
				}
			}
			if (targets.isEmpty() || depth == maxDepth) {
				break;
			}
			for (TestTrace trace : tracesOfCurrentDepth) {
				for (Target t : filterTempTargets(getAllOperationNames(), tempTargets)) {
					ConstraintBasedSequenceCheckCommand cmd = findTestPath(trace, t);
					if (cmd.getResult() != ConstraintBasedSequenceCheckCommand.ResultType.NO_PATH_FOUND && !visitedTargets.contains(t)) {
						Trace previousTrace = cmd.getTrace();
						cmd = findTestPathWithTarget(trace, t);
						Trace currentTrace = cmd.getTrace();
						TestTrace newTrace = trace.createNewTrace(trace.getTransitionNames(), t, // a trace is complete, i.e., should not be extended further if it contains a final operation or is statically proven to be infeasible
								(finalOperations.contains(t.getOperation()) || t.isInfeasible()), t.isInfeasible() ? previousTrace : currentTrace);
						testTraces.add(newTrace);
						visitedTargets.add(t);
					}
				}
				if(Thread.currentThread().isInterrupted()) {
					interrupted = true;
					break;
				}
			}
			depth++;
			if(Thread.currentThread().isInterrupted()) {
				interrupted = true;
				break;
			}
		}
		uncoveredTargets.addAll(targets);
		return new TestCaseGeneratorResult(testTraces, uncoveredTargets, infeasibleOperations, interrupted);
	}

	/**
	 * Determines the targets for the test case generation with MC/DC coverage.
	 *
	 * @param maxLevel The maximum level for MC/DC
	 * @return The targets.
	 */
	private List<Target> getMCDCTargets(int maxLevel) {
		List<Target> mcdcTargets = new ArrayList<>();
		Map<BEvent, List<ConcreteMCDCTestCase>> testCases = new MCDCIdentifier(stateSpace, maxLevel).identifyMCDC();
		for (Entry<BEvent, List<ConcreteMCDCTestCase>> entry : testCases.entrySet()) {
			for (ConcreteMCDCTestCase concreteMCDCTestCase : entry.getValue()) {
				// INITIALISATION is only added for Event-B model which should not be considered for test case generation
				if("INITIALISATION".equals(entry.getKey().getName())) {
					continue;
				}
				mcdcTargets.add(new Target(entry.getKey().getName(), concreteMCDCTestCase));
			}
		}
		return mcdcTargets;
	}

	/**
	 * Determines the {@link Target}s for the test case generation with operation coverage based on the selected operations.
	 *
	 * @param selectedOperations The list of selected operations
	 * @return The {@link Target}s
	 */
	private List<Target> getOperationCoverageTargets(List<String> selectedOperations) {
		return createTargetsForOperations(selectedOperations);
	}

	/**
	 * Creates {@link Target}s for a list of operations.
	 * <p>
	 * Each target consists of the operation's name and guard.
	 *
	 * @param operations The list of operations
	 * @return The {@link Target}s
	 */
	private List<Target> createTargetsForOperations(List<String> operations) {
		List<Target> operationTargets = new ArrayList<>();
		for (String operation : operations) {
			// INITIALISATION is only added for Event-B model which should not be considered for test case generation
			if("INITIALISATION".equals(operation)) {
				continue;
			}
			operationTargets.add(new Target(operation, getGuardAsPredicate(operation)));
		}
		return operationTargets;
	}

	/**
	 * Removes the targets which can never be reached due to an infeasible operation.
	 */
	private void discardInfeasibleTargets() {
		for (Target target : new ArrayList<>(targets)) {
			if (infeasibleOperations.contains(target.getOperation())) {
				uncoveredTargets.add(target);
				targets.remove(target);
			}
		}
	}

	/**
	 * Returns test traces that are of the specified depth and are not tagged as complete due to a final operation.
	 *
	 * @param traces All built traces
	 * @param depth  The current trace length
	 * @return List of paths that can be extended
	 */
	private List<TestTrace> filterDepthAndFinal(List<TestTrace> traces, int depth) {
		return traces.stream()
				.filter(x -> x.getDepth() == depth && !x.isComplete())
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Returns targets that have not yet been examined in the current iteration.
	 *
	 * @param operations  All feasible operations of the machine
	 * @param tempTargets The examined targets
	 * @return The artificial targets to be examined next, one for each valid operation
	 */
	private List<Target> filterTempTargets(List<String> operations, List<Target> tempTargets) {
		for (Target t : tempTargets) {
			operations.remove(t.getOperation());
		}
		return createTargetsForOperations(operations);
	}

	private PPredicate getGuardAsPredicate(String operation) {
		AbstractElement machine = stateSpace.getMainComponent();
		AbstractModel model = stateSpace.getModel();;

		List<IEvalElement> guards = Extraction.getGuardPredicates(machine, operation);
		ClassicalB predicate = (ClassicalB)Join.conjunct(model, guards);
		Start ast = predicate.getAst();
		return ((APredicateParseUnit) ast.getPParseUnit()).getPredicate();
	}

	/**
	 * Returns the names of all feasible operations.
	 *
	 * @return Names of feasible operations.
	 */
	private List<String> getAllOperationNames() {
		Machine machine = (Machine) stateSpace.getMainComponent();
		List<String> operations = new ArrayList<>();
		for (BEvent operation : machine.getEvents()) {
			if (!infeasibleOperations.contains(operation.getName())) {
				operations.add(operation.getName());
			}
		}
		return operations;
	}

	/**
	 * Executes the {@link ConstraintBasedSequenceCheckCommand}.
	 * <p>
	 * The command calls the ProB core to find a feasible path, composed of the transitions of a trace, that ends in a
	 * state that satisfies the guard of the regarded target.
	 *
	 * @param trace  The prior trace
	 * @param target The regarded target
	 * @return The command that contains the result of the ProB call
	 */
	private ConstraintBasedSequenceCheckCommand findTestPath(TestTrace trace, Target target) {
		// FIXME Can we use the original predicate AST instead of pretty-printing and re-parsing it?
		ClassicalB guardPredicate = new ClassicalB(target.getGuardString());
		ConstraintBasedSequenceCheckCommand cmd = new ConstraintBasedSequenceCheckCommand(stateSpace, trace.getTransitionNames(), guardPredicate);
		stateSpace.execute(cmd);
		return cmd;
	}

	/**
	 * Executes the {@link ConstraintBasedSequenceCheckCommand}.
	 * <p>
	 * The command calls the ProB core to find a feasible path containing the target as final operation.
	 * This function is used after checking the feasibility of the prior trace and the final operation.
	 *
	 * @param trace  The prior trace
	 * @param target The regarded target
	 * @return The command that contains the result of the ProB call
	 */
	private ConstraintBasedSequenceCheckCommand findTestPathWithTarget(TestTrace trace, Target target) {
		List<String> transitions = new ArrayList<>(trace.getTransitionNames());
		transitions.add(target.getOperation());
		ConstraintBasedSequenceCheckCommand cmd = new ConstraintBasedSequenceCheckCommand(stateSpace, transitions);
		stateSpace.execute(cmd);
		return cmd;
	}
}
