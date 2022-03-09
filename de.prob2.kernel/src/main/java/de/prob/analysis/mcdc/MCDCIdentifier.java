package de.prob.analysis.mcdc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.analysis.Conversion;
import de.prob.animator.command.CbcSolveCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.Join;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.Extraction;
import de.prob.model.representation.Machine;
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
		if (!(stateSpace.getMainComponent() instanceof Machine)) {
			return Collections.emptyMap();
		}

		Machine machine = (Machine)stateSpace.getMainComponent();
		AbstractModel model = stateSpace.getModel();

		Map<BEvent, List<ConcreteMCDCTestCase>> testCases = new HashMap<>();
		for (BEvent operation : machine.getEvents()) {
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
