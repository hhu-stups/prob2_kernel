package de.prob.check.tracereplay.check.renamig;

import java.util.Map;

import de.prob.prolog.term.CompoundPrologTerm;

public interface CheckerInterface {

	/**
	 * Represents a call to the kernel where two operations will be compared and a delta is extracted
	 * @param preparedOperation the prepared operation with free Prolog variables
	 * @param candidate the candidates to compare with
	 * @return a delta map, containing the delta between the two operations, or is empty if they are not connected
	 */
	Map<String, String> checkTypeII(PreparedOperation preparedOperation, CompoundPrologTerm candidate);
}
