package de.prob.check.tracereplay.check.exceptions;

import de.prob.prolog.term.PrologTerm;

public class PrologTermNotDefinedException extends Exception {

	final PrologTerm prologTerm;

	public PrologTermNotDefinedException(PrologTerm term){
		this.prologTerm = term;
	}
}
