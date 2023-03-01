package de.prob.check;

import de.prob.animator.domainobjects.CTL;

public class CTLNotYetFinished implements IModelCheckingResult {

	private final CTL formula;

	public CTLNotYetFinished(final CTL formula) {
		super();
		this.formula = formula;
	}

	@Override
	public String getMessage() {
		return "CTL checking not complete.";
	}

	public String getCode() {
		return formula.getCode();
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
