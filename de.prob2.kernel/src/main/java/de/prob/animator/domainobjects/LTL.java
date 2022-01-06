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
	
	/**
	 * Parse an LTL formula using the SableCC-based LTL parser.
	 * Predicates in the formula are parsed using the classical B parser
	 * (with no DEFINITIONS loaded).
	 * 
	 * @param code the LTL formula to parse
	 * @throws LtlParseException if parsing failed
	 */
	public LTL(final String code) throws LtlParseException {
		this(code, new ClassicalBParser());
	}
	
	/**
	 * Parse an LTL formula using the SableCC-based LTL parser.
	 * Predicates in the formula are parsed using the given language-specific parser.
	 * 
	 * @param code the LTL formula to parse
	 * @param languageSpecificParser the language-specific parser to use for predicates in the formula
	 * @throws LtlParseException if parsing failed
	 */
	public LTL(final String code, ProBParserBase languageSpecificParser)
			throws LtlParseException {
		this.code = code;
		generatedTerm = new de.be4.ltl.core.parser.LtlParser(languageSpecificParser)
							.generatePrologTerm(code, "root");
	}
	
	// FIXME This API is not very nice.
	// At the moment it is only used in the ProB 2 UI.
	// We should move some code from the UI to here
	// so that it is possible to parse LTL formulas using the ANTLR-based parser
	// without having to set up the entire parser by hand.
	/**
	 * Construct an LTL formula from an existing instance of the ANTLR-based LTL parser.
	 * 
	 * Note that this constructor does <i>not</i> actually parse anything!
	 * The calling code is expected to create an {@link LtlParser},
	 * call {@link LtlParser#parse()},
	 * and check that parsing was successful
	 * (using an error listener).
	 * Only then can this constructor be called successfully.
	 * 
	 * @param code the source code that was passed to the parser
	 *     (must be passed manually, because {@link LtlParser} doesn't allow retrieving the source code that it parsed)
	 * @param languageSpecificParser the language-specific parser to use for predicates in the formula
	 * @param parser an instance of the ANTLR-based LTL parser,
	 *     which must have already parsed successfully
	 * @throws ProBError if the passed parsed instance has not parsed yet or encountered errors
	 */
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
	
	/**
	 * Parse an LTL formula using the SableCC-based LTL parser.
	 * Predicates in the formula are parsed using the Event-B parser.
	 *
	 * @param formula the LTL formula to parse
	 * @throws LtlParseException if parsing failed
	 */
	public static LTL parseEventB(String formula) throws LtlParseException {
		return new LTL(formula, new EventBParserBase());
	}
	
	/**
	 * Parse an LTL formula using the SableCC-based LTL parser.
	 * Predicates in the formula are parsed using the classical B parser
	 * (with no DEFINITIONS loaded).
	 *
	 * @param formula the LTL formula to parse
	 * @throws LtlParseException if parsing failed
	 */
	public static LTL parseClassicalB(String formula) throws LtlParseException {
		return new LTL(formula, new ClassicalBParser());
	}
}
