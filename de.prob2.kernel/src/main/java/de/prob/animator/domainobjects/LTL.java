package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.ClassicalBParser;
import de.be4.ltl.core.parser.LtlParseException;
import de.prob.exception.ProBError;
import de.prob.ltl.parser.LtlParser;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class LTL {
	private final String code;
	private final PrologTerm generatedTerm;
	
	public LTL(final String code) throws LtlParseException {
		this(code, new ClassicalBParser());
	}

	public LTL(final String code, ProBParserBase languageSpecificParser)
			throws LtlParseException {
		this.code = code;
		generatedTerm = new de.be4.ltl.core.parser.LtlParser(languageSpecificParser)
							.generatePrologTerm(code, "root");
	}
	
	public LTL(final String code, ProBParserBase languageSpecificParser, LtlParser parser) {
		this.code = code;
		generatedTerm = parser.generatePrologTerm("root", languageSpecificParser);
		if (generatedTerm == null) {
			throw new ProBError("LTL Prolog term generation failed - most likely there was a parse error");
		}
	}
	
	public String getCode() {
		return this.code;
	}
	
	@Override
	public String toString() {
		return this.getCode();
	}
	
	public void printProlog(final IPrologTermOutput pout) {
		pout.printTerm(generatedTerm);
	}

	public static LTL parseEventB(String formula) throws LtlParseException {
		return new LTL(formula, new EventBParserBase());
	}

	public static LTL parseClassicalB(String formula) throws LtlParseException {
		return new LTL(formula, new ClassicalBParser());
	}
}
