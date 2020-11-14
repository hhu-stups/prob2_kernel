package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Returns the operations of the currently stored machine
 */
public class GetMachineOperationsFull extends AbstractCommand{

	private static final String PROLOG_COMMAND_NAME = "get_operations_and_names";
	public static final String VARIABLE = "OPS";
	public static final String VARIABLE2 = "NAMES";
	private Map<String, CompoundPrologTerm> ops;


	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printVariable(VARIABLE).printVariable(VARIABLE2).closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		ListPrologTerm operations = (ListPrologTerm) bindings.get(VARIABLE);
		ListPrologTerm operationNames = (ListPrologTerm) bindings.get(VARIABLE2);
		ops = new HashMap<>();
		for(int i = 0; i < operationNames.size(); i++){	
			ops.put(operationNames.get(i).getFunctor(), (CompoundPrologTerm) operations.get(i));
		}
	}

	public Map<String, CompoundPrologTerm> getOperationsWithNames(){
		return new HashMap<>(ops);
	}

	public List<CompoundPrologTerm> getOperations(){
		return new ArrayList<>(ops.values());
	}
}
