package de.prob.animator.domainobjects;

public enum FormulaTranslationMode {
	// These correspond to the modes accepted by translate:set_translation_mode/1 in probcli.
	ASCII("ascii"),
	UNICODE("unicode"),
	LATEX("latex"),
	ATELIERB("atelierb"),
	ATELIERB_PP("atelierb_pp"),
	;
	
	private final String prologName;
	
	private FormulaTranslationMode(final String prologName) {
		this.prologName = prologName;
	}
	
	public String getPrologName() {
		return this.prologName;
	}
}
