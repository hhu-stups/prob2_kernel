package de.prob.check.tracereplay.check.renamig;

import de.prob.prolog.term.CompoundPrologTerm;

public interface PrepareOperationsInterface {


	/**
	 * Prepares an Operation by replacing identifiers with free variables
	 * @param operation the operation on which the changes should be applied
	 * @return the prepared function
	 */
	PreparedOperation prepareOperation(CompoundPrologTerm operation) throws PrologTermNotDefinedException;
}
