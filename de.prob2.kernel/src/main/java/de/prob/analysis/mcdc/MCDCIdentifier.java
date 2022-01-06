package de.prob.analysis.mcdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.be4.classicalb.core.parser.node.*;
import de.prob.analysis.Conversion;
import de.prob.animator.command.CbcSolveCommand;
import de.prob.animator.domainobjects.*;
import de.prob.model.representation.Extraction;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.classicalb.Operation;
import de.prob.model.representation.*;
import de.prob.statespace.StateSpace;

/**
 * Determines the MCDC test cases for all guards of all operations of a given model extracted from the current state space up to a
 * specified {@link #maxLevel} (levels start at 0).
 */
public class MCDCIdentifier {

	private final static Logger log = Logger.getLogger(MCDCIdentifier.class.getName());

	private final StateSpace stateSpace;
	private final int maxLevel;

	public MCDCIdentifier(StateSpace stateSpace, int maxLevel) {
		this.stateSpace = stateSpace;
		this.maxLevel = maxLevel;
	}

	public Map<BEvent, List<ConcreteMCDCTestCase>> identifyMCDC() {
		AbstractElement machine = stateSpace.getMainComponent();
		AbstractModel model = stateSpace.getModel();

		Map<BEvent, List<ConcreteMCDCTestCase>> testCases = new HashMap<>();
		ModelElementList<BEvent> operations = machine.getChildrenOfType(BEvent.class);
		for (BEvent operation : operations) {
			List<IEvalElement> guards = Extraction.getGuardPredicates(machine, operation.getName());
			ClassicalB predicate;
			if(guards.isEmpty()) {
				predicate = new ClassicalB("1=1", FormulaExpand.EXPAND);
			} else {
				predicate = new ClassicalB(Join.conjunct(model, guards).getCode(), FormulaExpand.EXPAND);
			}
			Start ast = predicate.getAst();
			PPredicate startNode = ((APredicateParseUnit) ast.getPParseUnit()).getPredicate();
			testCases.put(operation, getMCDCTestCases(startNode));
		}
		return testCases;
	}

	private List<ConcreteMCDCTestCase> getMCDCTestCases(PPredicate node) {
		AbstractModel model = stateSpace.getModel();;

		List<ConcreteMCDCTestCase> testCases = new MCDCASTVisitor(maxLevel, model).getMCDCTestCases(node);
		return filterFeasible(testCases);
	}

	private List<ConcreteMCDCTestCase> filterFeasible(List<ConcreteMCDCTestCase> testCases) {
		AbstractModel model = stateSpace.getModel();

		List<ConcreteMCDCTestCase> feasibleTestCases = new ArrayList<>();
		for (ConcreteMCDCTestCase t : testCases) {
			CbcSolveCommand cmd = new CbcSolveCommand(Conversion.classicalBFromPredicate(model, t.getPredicate()));
			stateSpace.execute(cmd);
			if (cmd.getValue() == EvalResult.FALSE) {
				log.info("Infeasible: " + t.toString());
			} else {
				feasibleTestCases.add(t);
			}
		}
		return feasibleTestCases;
	}
}
