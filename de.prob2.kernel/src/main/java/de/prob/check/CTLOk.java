package de.prob.check;

import de.prob.animator.domainobjects.CTL;

public class CTLOk implements IModelCheckingResult {

	private final CTL ctl;

	public CTLOk(final CTL ctl) {
		super();
		this.ctl = ctl;
	}

	@Override
	public String getMessage() {
		return "CTL status for " + ctl.getCode() + " : ok";
	}

	public String getCode() {
		return ctl.getCode();
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
