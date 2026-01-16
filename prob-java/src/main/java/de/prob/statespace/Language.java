package de.prob.statespace;

public enum Language {
	CLASSICAL_B(FormalismType.B, null, "b", "B"),
	// B_RULES uses the same prologName as CLASSICAL_B - the Prolog side doesn't know anything about rules, all translation happens on the Java side.
	B_RULES(FormalismType.B, CLASSICAL_B, "b", "B Rules"),
	EVENT_B(FormalismType.B, null, "eventb", "Event-B"),
	TLA(FormalismType.B, CLASSICAL_B, "tla", "TLA+"),
	ALLOY(FormalismType.B, CLASSICAL_B, "alloy", "Alloy"),
	Z(FormalismType.Z, CLASSICAL_B, "z", "Z"),
	CSP(FormalismType.CSP, null, "cspm", "CSP"),
	XTL(FormalismType.XTL, null, "xtl", "XTL"),
	;
	
	private final FormalismType formalismType;
	private final Language translatedTo;
	private final String prologName;
	private final String prettyName;
	
	Language(final FormalismType formalismType, final Language translatedTo, final String prologName,
	         final String prettyName) {
		this.formalismType = formalismType;
		this.translatedTo = translatedTo;
		this.prologName = prologName;
		this.prettyName = prettyName;
	}
	
	public FormalismType getFormalismType() {
		return this.formalismType;
	}
	
	/**
	 * For languages that ProB supports by translating them to another language,
	 * returns the language that the model was internally translated to.
	 * 
	 * @return what this language is internally translated to,
	 *     or {@code null} if the language is supported natively by ProB
	 */
	public Language getTranslatedTo() {
		return this.translatedTo;
	}
	
	public String getPrologName() {
		return this.prologName;
	}

	public String getPrettyName() {
		return this.prettyName;
	}
}
