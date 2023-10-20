package de.prob.animator.domainobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.node.Node;
import de.prob.formula.TranslationVisitor;
import de.prob.model.representation.FormulaUUID;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.unicode.UnicodeTranslator;

import org.eventb.core.ast.ASTProblem;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.extension.IFormulaExtension;

/**
 * Representation of an Event-B formula
 *
 * @author joy
 *
 */
public class EventB extends AbstractEvalElement implements IBEvalElement {
	private final FormulaUUID uuid = new FormulaUUID();

	private EvalElementType kind;
	private Node ast = null;

	private final Set<IFormulaExtension> types;

	/**
	 * @param code
	 *            - The String which is a representation of the desired Event-B
	 *            formula
	 */
	public EventB(final String code) {
		this(code, Collections.emptySet());
	}

	public EventB(final String code, final FormulaExpand expand) {
		this(code, Collections.emptySet(), expand);
	}

	public EventB(final String code, final Set<IFormulaExtension> types) {
		this(code, types, FormulaExpand.EXPAND);
	}

	public EventB(final String code, final Set<IFormulaExtension> types, final FormulaExpand expansion) {
		super(UnicodeTranslator.toAscii(code), expansion);

		this.types = types;
	}

	/**
	 * <p>Create an Event-B formula representing the given identifier.</p>
	 * <p>
	 * Unlike the normal constructors that parse the input string using the Rodin parser,
	 * this method accepts arbitrary strings as identifiers,
	 * even ones that are not syntactically valid Event-B identifiers.
	 * </p>
	 *
	 * @param identifier list of string parts that make up a dotted identifier
	 * @param expansion expansion mode to use when evaluating the formula
	 * @return an Event-B formula representing the given identifier
	 */
	public static EventB fromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		final ClassicalB bFormula = ClassicalB.fromIdentifier(identifier, expansion);
		final EventB eventBFormula = new EventB(bFormula.getCode(), expansion);
		eventBFormula.ast = bFormula.getAst();
		eventBFormula.kind = EvalElementType.EXPRESSION;
		return eventBFormula;
	}
	
	/**
	 * <p>Create an Event-B formula representing the given identifier.</p>
	 * <p>
	 * Unlike the normal constructors that parse the input string using the Rodin parser,
	 * this method accepts arbitrary strings as identifiers,
	 * even ones that are not syntactically valid Event-B identifiers.
	 * </p>
	 *
	 * @param identifier list of string parts that make up a dotted identifier
	 * @return an Event-B formula representing the given identifier
	 */
	public static EventB fromIdentifier(final List<String> identifier) {
		return fromIdentifier(identifier, FormulaExpand.TRUNCATE);
	}
	
	public IParseResult ensurePredicateParsed() {
		final String unicode = this.toUnicode();
		kind = EvalElementType.PREDICATE;
		IParseResult parseResult = FormulaFactory.getInstance(types)
				.parsePredicate(unicode, null);
		if(!parseResult.hasProblem()) {
			ast = preparePredicateAst(parseResult);
		}
		return parseResult;
	}
	
	public IParseResult ensureExpressionParsed() {
		final String unicode = this.toUnicode();
		kind = EvalElementType.EXPRESSION;
		IParseResult parseResult = FormulaFactory.getInstance(types)
				.parseExpression(unicode, null);
		if(!parseResult.hasProblem()) {
			ast = prepareExpressionAst(parseResult);
		}
		return parseResult;
	}
	
	public IParseResult ensureAssignmentParsed() {
		final String unicode = this.toUnicode();
		kind = EvalElementType.ASSIGNMENT;
		IParseResult parseResult = FormulaFactory.getInstance(types)
				.parseAssignment(unicode, null);
		if(!parseResult.hasProblem()) {
			ast = prepareAssignmentAst(parseResult);
		}
		return parseResult;
	}

	public void ensureParsed() {
		// TO DO: provide constructor/method for choosing the kind beforehand, or at least disabling substitution parsing (which is rarely necessary)
		IParseResult parseResult = ensurePredicateParsed();
		List<String> errors = new ArrayList<>();
		if(parseResult.hasProblem()) {
			errors.add("Parsing predicate failed because: " + parseResult);
			addProblems(parseResult, errors);
			parseResult = ensureExpressionParsed();
			if(parseResult.hasProblem()) {
				errors.add("Parsing expression failed because: " + parseResult);
				addProblems(parseResult, errors);
				parseResult = ensureAssignmentParsed();
				if (parseResult.hasProblem()) {
					errors.add("Parsing substitution failed because: " + parseResult);
					addProblems(parseResult, errors);
					kind = EvalElementType.NONE;
				}
			}
		}
		
		if(parseResult.hasProblem()) {
			errors.add("Code: " + this.getCode());
			errors.add("Unicode translation: " + this.toUnicode());
			throw new EvaluationException("Could not parse formula:\n" + String.join("\n", errors));
		}
	}
	
	private void addProblems(final IParseResult parseResult, List<String> errors) {
		for (final ASTProblem problem : parseResult.getProblems()) {
			errors.add(problem.toString());
		}
	}

	private Node prepareAssignmentAst(final IParseResult parseResult) {
		final Assignment assign = parseResult.getParsedAssignment();
		final TranslationVisitor visitor = new TranslationVisitor();
		try {
			assign.accept(visitor);
		} catch (RuntimeException e) {
			throw new EvaluationException("Could not create AST for assignment " + assign + "\nCode: " + this.getCode(), e);
		}
		return visitor.getSubstitution();
	}

	private Node prepareExpressionAst(final IParseResult parseResult) {
		final Expression expr = parseResult.getParsedExpression();
		final TranslationVisitor visitor = new TranslationVisitor();
		try {
			expr.accept(visitor);
		} catch (RuntimeException e) {
			throw new EvaluationException("Could not create AST for expression " + expr + "\nCode: " + this.getCode(), e);
		}
		return visitor.getExpression();
	}

	private Node preparePredicateAst(final IParseResult parseResult) {
		final Predicate parsedPredicate = parseResult.getParsedPredicate();
		final TranslationVisitor visitor = new TranslationVisitor();
		try {
			parsedPredicate.accept(visitor);
		} catch (RuntimeException e) {
			throw new EvaluationException("Could not create AST for predicate " + parsedPredicate + "\nCode: " + this.getCode(), e);
		}
		return visitor.getPredicate();
	}

	@Override
	public void printProlog(final IPrologTermOutput pout) {
		if (ast == null) {
			ensureParsed();
		}
		if (EvalElementType.ASSIGNMENT.equals(getKind())) {
			throw new EvaluationException(
					"Assignments are currently unsupported for evaluation");
		}

		assert ast != null;
		final ASTProlog prolog = new ASTProlog(pout, null);
		ast.apply(prolog);
	}

	@Override
	public void printEvalTerm(final IPrologTermOutput pout) {
		if (ast == null) {
			ensureParsed();
		}
		super.printEvalTerm(pout);
	}

	@Override
	public EvalElementType getKind() {
		if (kind == null) {
			try {
				ensureParsed();
			} catch (EvaluationException exc) {
				// ensureParsed should always set the kind, even if it throws an EvaluationException.
				if (kind == null) {
					throw new AssertionError("ensureParsed didn't initialize the formula kind", exc);
				}
			}
			if (kind == null) {
				throw new AssertionError("ensureParsed didn't initialize the formula kind");
			}
		}
		return kind;
	}

	@Override
	public Node getAst() {
		if (ast == null) {
			ensureParsed();
		}

		assert ast != null;

		return ast;
	}

	@Override
	public IFormulaUUID getFormulaId() {
		return uuid;
	}

	public String toUnicode() {
		return UnicodeTranslator.toRodinUnicode(this.getCode());
	}

	public IParseResult getRodinParsedResult() {
		if (kind == null) {
			ensureParsed();
		}
		switch (kind) {
			case PREDICATE:
				return FormulaFactory.getInstance(types).parsePredicate(toUnicode(), null);
			
			case EXPRESSION:
				return FormulaFactory.getInstance(types).parseExpression(toUnicode(), null);
			
			case ASSIGNMENT:
				return FormulaFactory.getInstance(types).parseAssignment(toUnicode(), null);
			
			default:
				throw new IllegalStateException("Unhandled kind: " + kind);
		}
	}

	public Set<IFormulaExtension> getTypes() {
		return types;
	}
}
