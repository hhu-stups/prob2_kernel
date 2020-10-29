package de.prob.animator.command;


import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * Calls the prolog site to prepare an operation and contains the prepared operation and the extracted variables afterwards
 */
public class PrepareOperations extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prepare_operations";
	public static final String VARIABLE1 = "PREPARED_OPERATION";
	public static final String VARIABLE2 = "FOUND_VARS";
	public static final String VARIABLE3 = "FREE_VARS";


	private ListPrologTerm foundVars;
	private ListPrologTerm freeVars;
	private CompoundPrologTerm preparedOperation;

	private final CompoundPrologTerm operationOld;


	public PrepareOperations(CompoundPrologTerm operationOld){
		this.operationOld = operationOld;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {

		pto.openTerm(PROLOG_COMMAND_NAME).printTerm(operationOld).printVariable(VARIABLE1).printVariable(VARIABLE2).printVariable(VARIABLE3).closeTerm();
	}


	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		foundVars = (ListPrologTerm) bindings.get(VARIABLE2);
		preparedOperation = (CompoundPrologTerm) bindings.get(VARIABLE1);
		freeVars = (ListPrologTerm) bindings.get(VARIABLE3);
	}


	public ListPrologTerm getFoundVars() {
		return foundVars;
	}

	public ListPrologTerm getFreeVars() {
		return freeVars;
	}

	public CompoundPrologTerm getPreparedOperation() {
		return preparedOperation;
	}

}
