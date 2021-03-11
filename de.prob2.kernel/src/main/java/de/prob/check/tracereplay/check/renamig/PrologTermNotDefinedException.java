package de.prob.check.tracereplay.check.renamig;

import de.prob.prolog.term.PrologTerm;

public class PrologTermNotDefinedException extends Exception {

	final PrologTerm prologTerm;

	public PrologTermNotDefinedException(PrologTerm term){
		this.prologTerm = term;
	}


	@Override
	public String getMessage() {
		return "A part of the prolog syntax is not yet implemented.";
	}

	@Override
	public String toString() {
		return "Missing implementation of: " +  prologTerm.toString();
	}
}
