package de.prob.check.tracereplay.check;

import de.prob.animator.command.PrepareOperations;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.statespace.StateSpace;

public interface PrepareOperationsInterface {


	/**
	 * Prepares an Operation by replacing identifiers with free variables
	 * @param operation the operation on which the changes should be applied
	 * @return the prepared function
	 */
	Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> prepareOperation(CompoundPrologTerm operation);
}
