package de.prob.model.representation;

/**
 * Common interface for B components that may contain constants,
 * i. e. a classical B machine or an Event-B context.
 */
public interface ConstantsComponent extends Named {
	ModelElementList<Set> getSets();
	ModelElementList<? extends Constant> getConstants();
	ModelElementList<? extends Axiom> getAxioms();
	// TODO Perhaps also add getAllAxioms (currently only implemented for Event-B)?
}
