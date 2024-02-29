package de.prob.model.representation;

import de.prob.prolog.output.IPrologTermOutput;

public interface IFormulaUUID {
	void printUUID(IPrologTermOutput pto);

	String getUUID();
}
