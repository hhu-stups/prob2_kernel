package de.prob.analysis;

import java.util.ArrayList;
import java.util.List;

import de.prob.animator.command.CbcSolveCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.Join;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.classicalb.Operation;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.Extraction;
import de.prob.model.representation.Machine;
import de.prob.statespace.StateSpace;

public class FeasibilityAnalysis {

	private final StateSpace stateSpace;

	public FeasibilityAnalysis(StateSpace stateSpace) {
		this.stateSpace = stateSpace;
	}

	public List<String> analyseFeasibility() {
		AbstractElement machine = stateSpace.getMainComponent();
		AbstractModel model = stateSpace.getModel();

		List<String> infeasibleOperations = new ArrayList<>();
		List<IEvalElement> invariantPredicates = Extraction.getInvariantPredicates(machine);
		for (BEvent operation : machine.getChildrenOfType(BEvent.class)) {
			List<IEvalElement> iEvalElements = new ArrayList<>(invariantPredicates);
			iEvalElements.addAll(Extraction.getGuardPredicates(machine, operation.getName()));
			ClassicalB predicate;
			if(iEvalElements.isEmpty()) {
				predicate = new ClassicalB("1=1", FormulaExpand.EXPAND);
			} else {
				predicate = new ClassicalB(Join.conjunct(model, iEvalElements).getCode(), FormulaExpand.EXPAND);
			}
			CbcSolveCommand cmd = new CbcSolveCommand(predicate);
			stateSpace.execute(cmd);
			
			if (!(cmd.getValue() instanceof EvalResult) || !(((EvalResult) cmd.getValue()).getValue().equals("TRUE"))) {
				infeasibleOperations.add(operation.getName());
			}
		}
		return infeasibleOperations;
	}
}
