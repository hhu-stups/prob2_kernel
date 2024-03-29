package de.prob.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.animator.command.CbcSolveCommand;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.Join;
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
		if (!(stateSpace.getMainComponent() instanceof Machine)) {
			return Collections.emptyList();
		}

		Machine machine = (Machine)stateSpace.getMainComponent();
		AbstractModel model = stateSpace.getModel();

		List<String> infeasibleOperations = new ArrayList<>();
		List<IEvalElement> invariantPredicates = Extraction.getInvariantPredicates(machine);
		for (BEvent operation : machine.getEvents()) {
			List<IEvalElement> iEvalElements = new ArrayList<>(invariantPredicates);
			iEvalElements.addAll(Extraction.getGuardPredicates(machine, operation.getName()));
			CbcSolveCommand cmd = new CbcSolveCommand(Join.conjunct(model, iEvalElements));
			stateSpace.execute(cmd);
			
			if (!(cmd.getValue() instanceof EvalResult) || !(((EvalResult) cmd.getValue()).getValue().equals("TRUE"))) {
				infeasibleOperations.add(operation.getName());
			}
		}
		return infeasibleOperations;
	}
}
