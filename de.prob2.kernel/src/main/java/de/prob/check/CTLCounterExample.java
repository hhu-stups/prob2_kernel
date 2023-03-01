package de.prob.check;

import de.prob.animator.domainobjects.CTL;

public class CTLCounterExample implements IModelCheckingResult {

	private final CTL ctl;

	public CTLCounterExample(final CTL ctl) {
		super();
		this.ctl = ctl;
	}

	@Override
	public String getMessage() {
		return "CTL counterexample";
	}

	public String getCode() {
		return ctl.getCode();
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
