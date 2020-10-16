package de.prob.animator.command;

import com.sun.tools.javac.code.Attribute;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Returns the operations of the currently stored machine
 */
public class GetMachineOperationsFull extends AbstractCommand{

	private static final String PROLOG_COMMAND_NAME = "get_operations";
	public static final String VARIABLE = "OPS";
	private List<CompoundPrologTerm> ops;


	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printVariable(VARIABLE).closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		ListPrologTerm listPrologTerm = (ListPrologTerm) bindings.get(VARIABLE);
		ops = listPrologTerm.stream().map(element -> (CompoundPrologTerm) element).collect(Collectors.toList());
	}


	public List<CompoundPrologTerm> getOperations(){
		return ops;
	}
}
