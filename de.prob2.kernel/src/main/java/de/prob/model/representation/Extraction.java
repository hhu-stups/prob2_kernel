package de.prob.model.representation;

import java.util.ArrayList;
import java.util.List;

import de.prob.animator.domainobjects.IEvalElement;

/**
 * Contains methods for tidy access to different types of {@link IEvalElement}s.
 */
public final class Extraction {

	private Extraction() {
	}

	public static List<IEvalElement> getInvariantPredicates(AbstractElement machine) {
		List<IEvalElement> iEvalElements = new ArrayList<>();
		if (!(machine instanceof Machine)) {
			return iEvalElements;
		}
		for (Invariant invariant : ((Machine)machine).getInvariants()) {
			iEvalElements.add(invariant.getPredicate());
		}
		return iEvalElements;
	}

	public static List<IEvalElement> getGuardPredicates(AbstractElement machine, String operationName) {
		List<IEvalElement> iEvalElements = new ArrayList<>();
		if (!(machine instanceof Machine)) {
			return iEvalElements;
		}
		BEvent operation = ((Machine)machine).getEvent(operationName);
		if(operation == null || operation.getChildren() == null || operation.getChildren().get(Guard.class) == null) {
			return iEvalElements;
		}
		for (AbstractElement guard : operation.getChildren().get(Guard.class)) {
			iEvalElements.add(((Guard) guard).getPredicate());
		}
		return iEvalElements;
	}
}
