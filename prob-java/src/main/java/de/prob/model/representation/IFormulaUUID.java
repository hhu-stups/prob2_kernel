package de.prob.model.representation;

import de.prob.prolog.output.IPrologTermOutput;

public interface IFormulaUUID {

	default void printUUID(IPrologTermOutput pto) {
		pto.printAtom(this.getUUID());
	}

	String getUUID();
}
