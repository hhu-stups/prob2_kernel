/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.TranslatingVisitor;
import de.prob.model.representation.FormulaUUID;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;

/**
 * Representation of a ClassicalB formula.
 * 
 * @author joy
 */
public class ClassicalB extends AbstractEvalElement implements IBEvalElement {

	private final FormulaUUID uuid = new FormulaUUID();

	private final Start ast;

	public ClassicalB(final Start ast, final FormulaExpand expansion, final String code) {
		super(code, expansion);

		this.ast = ast;
	}

	public ClassicalB(final Start ast, final FormulaExpand expansion) {
		this(ast, expansion, prettyprint(ast));
	}

	/**
	 * @param ast
	 *            is saved and the string representation determined from the ast
	 *            and saved
	 * @deprecated Use {@link #ClassicalB(Start, FormulaExpand)} with an explicit {@link FormulaExpand} argument instead
	 */
	@Deprecated
	public ClassicalB(final Start ast) {
		this(ast, FormulaExpand.TRUNCATE);
	}

	public ClassicalB(final String formula, final FormulaExpand expansion) {
		this(parse(formula,false), expansion, formula); // false: does not allow substitutions
	}
	public ClassicalB(final String formula, final FormulaExpand expansion, final Boolean AllowSubst) {
		this(parse(formula,AllowSubst), expansion, formula);
	}

	/**
	 * @param code
	 *            will be parsed and the resulting {@link Start} ast saved
	 * @throws EvaluationException
	 *             if the code could not be parsed
	 * @deprecated Use {@link #ClassicalB(String, FormulaExpand)} with an explicit {@link FormulaExpand} argument instead
	 */
	@Deprecated
	public ClassicalB(final String code) {
		this(code, FormulaExpand.EXPAND);
	}

	private static Start parse(final String formula, Boolean AllowSubst) {
		final BParser bParser = new BParser();
		try {
			return bParser.parseFormula(formula);
		} catch (BCompoundException e) {
		    if (AllowSubst) {
		       // also try parsing as substitution
				try {
					return bParser.parseSubstitution(formula);
				} catch (BCompoundException f) {
					throw new EvaluationException(f.getMessage(), f);
				}
			} else {
				throw new EvaluationException(e.getMessage(), e);
			}
		}
	}

	private static String prettyprint(final Node predicate) {
		final PrettyPrinter prettyPrinter = new PrettyPrinter();
		predicate.apply(prettyPrinter);
		return prettyPrinter.getPrettyPrint();
	}

	@Override
	public EvalElementType getKind() {
		if (ast.getPParseUnit() instanceof AExpressionParseUnit) {
			return EvalElementType.EXPRESSION;
		} else if (ast.getPParseUnit() instanceof APredicateParseUnit) {
			return EvalElementType.PREDICATE;
		} else {
			return EvalElementType.ASSIGNMENT;
		}
	}

	/**
	 * @return {@link Start} ast corresponding to the formula
	 */
	@Override
	public Start getAst() {
		return ast;
	}

	@Override
	public void printProlog(final IPrologTermOutput pout) {
		if (EvalElementType.ASSIGNMENT.equals(getKind())) {
			throw new EvaluationException("Substitutions are currently unsupported for evaluation");
		}
		if (ast.getEOF() == null) {
			ast.setEOF(new EOF());
		}
		ASTProlog.printFormula(ast, pout);
	}

	@Override
	public String serialized() {
		return "#ClassicalB:" + this.getCode();
	}

	@Override
	public IFormulaUUID getFormulaId() {
		return uuid;
	}

	@Override
	public <T extends BValue> T translate() {
		if (!EvalElementType.EXPRESSION.equals(getKind())) {
			throw new IllegalArgumentException();
		}
		TranslatingVisitor<T> v = new TranslatingVisitor<>();
		getAst().apply(v);
		return v.getResult();
	}
}
