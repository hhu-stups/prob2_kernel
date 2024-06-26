/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.domainobjects;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.ASubstitutionParseUnit;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.PParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.model.representation.FormulaUUID;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;

/**
 * Representation of a ClassicalB formula.
 *
 * @author joy
 */
public final class ClassicalB extends AbstractEvalElement implements IBEvalElement {

	private final FormulaUUID uuid = new FormulaUUID();
	private final Start ast;
	private String cachedCode;

	public ClassicalB(final Start ast, final FormulaExpand expansion, final String code) {
		super(null, expansion);

		this.cachedCode = code;

		if (ast == null) {
			// If ast is null, parse it from code, which must not be null.
			if (code == null) {
				throw new IllegalArgumentException("both ast and code are null");
			}
			// The code is parsed eagerly so that any parse errors are thrown from the constructor,
			// not from later code that tries to write the formula to Prolog or otherwise uses the AST.
			this.ast = parse(code, false);
		} else {
			// If ast is non-null, then code may or may not be null.
			// If code is null, it will be initialized lazily by pretty-printing the AST.
			// If code is non-null, it's assumed to be the original source code or an existing pretty-print of the AST.
			this.ast = ast;
		}
	}

	public ClassicalB(final Start ast, final String code) {
		this(ast, FormulaExpand.TRUNCATE, code);
	}

	public ClassicalB(final Start ast, final FormulaExpand expansion) {
		this(Objects.requireNonNull(ast, "ast"), expansion, null);
	}

	/**
	 * @param ast is saved and the string representation determined from the ast
	 *            and saved
	 */
	public ClassicalB(final Start ast) {
		this(ast, FormulaExpand.TRUNCATE);
	}

	public ClassicalB(final String formula, final FormulaExpand expansion) {
		this(null, expansion, Objects.requireNonNull(formula, "formula"));
	}

	/**
	 * @deprecated Parsing substitutions into an {@link IEvalElement} is not useful at the moment,
	 * because they cannot be evaluated.
	 * If you only need to work with expressions and predicates,
	 * use {@link #ClassicalB(String)} instead.
	 * To parse a substitution,
	 * use {@link BParser#parseSubstitution(String)} instead
	 * (you can then pass the parsed AST into {@link #ClassicalB(Start, String)} if you really need to for some reason).
	 */
	@Deprecated
	public ClassicalB(final String formula, final FormulaExpand expansion, final boolean allowSubstitution) {
		this(parse(Objects.requireNonNull(formula, "formula"), allowSubstitution), expansion, formula);
	}

	/**
	 * @param code will be parsed and the resulting {@link Start} ast saved
	 * @throws EvaluationException if the code could not be parsed
	 */
	public ClassicalB(final String code) {
		this(code, FormulaExpand.EXPAND);
	}

	private static Start parse(String formula, boolean allowSubstitution) {
		final BParser bParser = new BParser();
		try {
			return bParser.parseFormula(formula);
		} catch (BCompoundException e) {
			if (allowSubstitution) {
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

	/**
	 * <p>Create a classical B formula representing the given identifier.</p>
	 * <p>
	 * Unlike the normal constructors that parse the input string using the B parser,
	 * this method accepts arbitrary strings as identifiers,
	 * even ones that are not syntactically valid B identifiers
	 * and would otherwise need to be quoted.
	 * </p>
	 *
	 * @param identifier list of string parts that make up a dotted identifier
	 * @param expansion  expansion mode to use when evaluating the formula
	 * @return a classical B formula representing the given identifier
	 */
	public static ClassicalB fromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		final AIdentifierExpression idNode = new AIdentifierExpression(identifier.stream().map(TIdentifierLiteral::new).collect(Collectors.toList()));
		final Start ast = new Start(new AExpressionParseUnit(idNode), new EOF());
		return new ClassicalB(ast, expansion);
	}

	/**
	 * <p>Create a classical B formula representing the given identifier.</p>
	 * <p>
	 * Unlike the normal constructors that parse the input string using the B parser,
	 * this method accepts arbitrary strings as identifiers,
	 * even ones that are not syntactically valid B identifiers
	 * and would otherwise need to be quoted.
	 * </p>
	 *
	 * @param identifier list of string parts that make up a dotted identifier
	 * @return a classical B formula representing the given identifier
	 */
	public static ClassicalB fromIdentifier(final List<String> identifier) {
		return fromIdentifier(identifier, FormulaExpand.TRUNCATE);
	}

	@Override
	public String getCode() {
		if (this.cachedCode == null) {
			final PrettyPrinter prettyPrinter = new PrettyPrinter();
			this.ast.apply(prettyPrinter);
			this.cachedCode = prettyPrinter.getPrettyPrint();
		}

		return this.cachedCode;
	}

	@Override
	public EvalElementType getKind() {
		PParseUnit parseUnit = this.getAst().getPParseUnit();
		if (parseUnit instanceof AExpressionParseUnit) {
			return EvalElementType.EXPRESSION;
		} else if (parseUnit instanceof APredicateParseUnit) {
			return EvalElementType.PREDICATE;
		} else if (parseUnit instanceof ASubstitutionParseUnit) {
			return EvalElementType.ASSIGNMENT;
		} else {
			throw new IllegalStateException("unknown kind for ClassicalB: " + parseUnit.getClass().getSimpleName() + " " + parseUnit);
		}
	}

	/**
	 * @return {@link Start} ast corresponding to the formula
	 */
	@Override
	public Start getAst() {
		return this.ast;
	}

	@Override
	public void printProlog(final IPrologTermOutput pout) {
		if (EvalElementType.ASSIGNMENT.equals(this.getKind())) {
			throw new EvaluationException("Substitutions are currently unsupported for evaluation");
		}

		ASTProlog.printFormula(this.getAst(), pout);
	}

	@Override
	public IFormulaUUID getFormulaId() {
		return this.uuid;
	}
}
