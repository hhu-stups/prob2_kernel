package de.prob.check.tracereplay.check.renamig;

import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;

public interface PrepareOperationsInterface {


	/**
	 * Prepares an Operation by replacing identifiers with free variables
	 * @param operation the operation on which the changes should be applied
	 * @return the prepared function
	 */
	Triple<ListPrologTerm, ListPrologTerm, CompoundPrologTerm> prepareOperation(CompoundPrologTerm operation) throws PrologTermNotDefinedException;
}
