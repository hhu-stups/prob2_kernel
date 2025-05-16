package de.prob.cli;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermDelegate;

final class GroundReprTermOutput extends PrologTermDelegate {

	private static final String GROUND_VAR_FUNCTOR = "_VAR";

	GroundReprTermOutput(IPrologTermOutput pto) {
		super(pto);
	}

	@Override
	public IPrologTermOutput printVariable(String var) {
		return this.openTerm(GROUND_VAR_FUNCTOR).printAtom(var).closeTerm();
	}

	@Override
	public IPrologTermOutput printAnonVariable() {
		throw new UnsupportedOperationException("unnamed variables not supported");
	}
}
