package de.prob.check;

import de.prob.animator.domainobjects.CTL;

public class CTLCouldNotDecide implements IModelCheckingResult {

	private final CTL formula;

	public CTLCouldNotDecide(final CTL formula) {
		super();
		this.formula = formula;
	}

	@Override
	public String getMessage() {
		return "CTL checking could not decide property: " + formula.getCode();
	}

	public String getCode() {
		return formula.getCode();
	}

	@Override
	public String toString() {
		return getMessage();
	}
}
