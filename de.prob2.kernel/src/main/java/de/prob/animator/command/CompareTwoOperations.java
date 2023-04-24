package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

/**
 * Hands down two operations to prolog and as a result gets a delta when both operation are similar in name, parameter name
 * or a no when operations are not similar according to these metrics.
 */
public class CompareTwoOperations extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "compare_operations";

	private final CompoundPrologTerm operationOld;
	private final CompoundPrologTerm operationNew;
	private final ListPrologTerm freeVariables;

	private List<String> identifiers;

	public CompareTwoOperations(CompoundPrologTerm operationOld, CompoundPrologTerm operationNew, ListPrologTerm freeVars) {
		this.operationNew = operationNew;
		this.operationOld = operationOld;
		this.freeVariables = freeVars;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printTerm(operationOld).printTerm(operationNew).printTerm(freeVariables).closeTerm();
	}


	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		this.identifiers = new ArrayList<>();
		for (final PrologTerm term : this.freeVariables) {
			final VariablePrologTerm var = (VariablePrologTerm)term;
			this.identifiers.add(bindings.get(var.getName()).atomToString());
		}
	}
	
	public List<String> getIdentifiers() {
		return Collections.unmodifiableList(this.identifiers);
	}
}
