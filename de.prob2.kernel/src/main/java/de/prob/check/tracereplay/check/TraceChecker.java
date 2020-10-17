package de.prob.check.tracereplay.check;


import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.LoadedMachine;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Performs static checks on a trace
 */
public class TraceChecker {

	private final PersistentTrace trace;
	private final LoadedMachine oldMachine;
	private final LoadedMachine newMachine;
	private TraceDiagnostic traceDiagnostic;

	public TraceChecker(PersistentTrace trace, LoadedMachine oldMachine, LoadedMachine newMachine){
		this.trace = trace;
		this.newMachine = newMachine;
		this.oldMachine = oldMachine;
	}

	/**
	 * Container to store check results
	 */
	class TraceDiagnostic{
		Set<String> missingOperations;

	}

	public void check(){
		Set<String> usedOperation = usedOperations(trace);
		Set<String> usedOperationsNoInit = new HashSet<>(usedOperation);
		usedOperationsNoInit.remove("$initialisation");
		if(usedOperationsNoInit.equals(oldMachine.getOperationNames())){


			if(parameterPerOperation(usedOperationsNoInit, oldMachine).equals(parameterPerOperation(usedOperationsNoInit, newMachine))){

			}

		}
	}


	/**
	 * Takes a set of operation names and maps the corresponding used parameters to it
	 * @param operations the operation names to map to
	 * @param loadedMachine the target where one gets the parameters from
	 * @return a map representing a Set of parameter names used by the given operations
	 */
	public Map<String, Set<String>> parameterPerOperation(Set<String> operations, LoadedMachine loadedMachine){
		return operations.stream().collect(Collectors.toMap(entry -> entry , entry -> {
			List<String> outputVars = loadedMachine.getOperations().get(entry).getOutputParameterNames();
			List<String> inputVars = loadedMachine.getOperations().get(entry).getParameterNames();
			List<String> all = new ArrayList<>();
			all.addAll(outputVars);
			all.addAll(inputVars);
			return new HashSet<>(all);
		}));
	}

	/**
	 * Returns the operations actually used by the trace, contains $initialisation
	 * @param trace the trace to analyse
	 * @return a set of operations used in the trace
	 */
	public Set<String> usedOperations(PersistentTrace trace){
		return trace.getTransitionList().stream().map(PersistentTransition::getOperationName).collect(Collectors.toSet());
	}

}
