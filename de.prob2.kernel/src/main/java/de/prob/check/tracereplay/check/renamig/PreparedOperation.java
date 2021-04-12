package de.prob.check.tracereplay.check.renamig;

import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;

public final class PreparedOperation {
	private final ListPrologTerm foundVariables;
	private final ListPrologTerm freeVariables;
	private final CompoundPrologTerm preparedAst;
	
	public PreparedOperation(final ListPrologTerm foundVariables, final ListPrologTerm freeVariables, final CompoundPrologTerm preparedAst) {
		this.foundVariables = foundVariables;
		this.freeVariables = freeVariables;
		this.preparedAst = preparedAst;
	}
	
	public ListPrologTerm getFoundVariables() {
		return this.foundVariables;
	}
	
	public ListPrologTerm getFreeVariables() {
		return this.freeVariables;
	}
	
	public CompoundPrologTerm getPreparedAst() {
		return this.preparedAst;
	}
}
