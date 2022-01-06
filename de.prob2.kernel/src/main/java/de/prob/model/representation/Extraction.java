package de.prob.model.representation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.classicalb.ClassicalBInvariant;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.classicalb.Operation;

/**
 * Contains methods for tidy access to different types of {@link IEvalElement}s.
 */
public final class Extraction {

	private Extraction() {
	}

	public static List<IEvalElement> getInvariantPredicates(AbstractElement machine) {
		List<IEvalElement> iEvalElements = new ArrayList<>();
		ModelElementList<Invariant> invariants = machine.getChildrenOfType(Invariant.class);
		if(invariants == null) {
			return iEvalElements;
		}
		for (Invariant invariant : invariants) {
			iEvalElements.add(invariant.getPredicate());
		}
		return iEvalElements;
	}

	public static List<IEvalElement> getGuardPredicates(AbstractElement machine, String operationName) {
		List<IEvalElement> iEvalElements = new ArrayList<>();
		List<BEvent> events = machine.getChildrenOfType(BEvent.class).stream()
				.filter(child -> operationName.equals(child.getName()))
				.collect(Collectors.toList());
		System.out.println(machine.getClass());
		System.out.println(machine.getChildren());
		System.out.println(machine.getChildrenOfType(BEvent.class));
		System.out.println(events);
		System.out.println("**********");
		if(events.isEmpty()) {
			return iEvalElements;
		}
		BEvent operation = events.get(0);
		if(operation == null || operation.getChildren() == null || operation.getChildren().get(Guard.class) == null) {
			return iEvalElements;
		}
		for (AbstractElement guard : operation.getChildren().get(Guard.class)) {
			iEvalElements.add(((Guard) guard).getPredicate());
		}
		return iEvalElements;
	}
}
