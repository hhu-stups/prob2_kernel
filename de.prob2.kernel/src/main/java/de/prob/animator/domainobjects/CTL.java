package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.ClassicalBParser;
import de.be4.ltl.core.parser.CtlParser;
import de.be4.ltl.core.parser.LtlParseException;
import de.prob.exception.ProBError;
import de.prob.ltl.parser.LtlParser;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class CTL {
	private final String code;
	private final PrologTerm generatedTerm;

	public CTL(final String code) throws LtlParseException {
		this(code, new ClassicalBParser());
	}

	public CTL(final String code, ProBParserBase languageSpecificParser) throws LtlParseException {
		this.code = code;
		generatedTerm = new de.be4.ltl.core.parser.CtlParser(languageSpecificParser)
							.generatePrologTerm(code, "root");
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

	public static CTL parseEventB(String formula) throws LtlParseException {
		return new CTL(formula, new EventBParserBase());
	}
	

	public static CTL parseClassicalB(String formula) throws LtlParseException {
		return new CTL(formula, new ClassicalBParser());
	}
}
