package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.formula.PredicateBuilder;

public interface PredicateBuilderFactoryInterface {

	PredicateBuilder createPredicateBuilder(PersistentTransition transition);
}
