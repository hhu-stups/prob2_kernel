package de.prob.model.classicalb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.node.AAssertionsMachineClause;
import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.AConstantsMachineClause;
import de.be4.classicalb.core.parser.node.AConstraintsMachineClause;
import de.be4.classicalb.core.parser.node.ADeferredSetSet;
import de.be4.classicalb.core.parser.node.ADescriptionExpression;
import de.be4.classicalb.core.parser.node.AEnumeratedSetSet;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AInvariantMachineClause;
import de.be4.classicalb.core.parser.node.ALocalOperationsMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AOperation;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.APreconditionSubstitution;
import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.ASelectSubstitution;
import de.be4.classicalb.core.parser.node.ASetsMachineClause;
import de.be4.classicalb.core.parser.node.ASubstitutionParseUnit;
import de.be4.classicalb.core.parser.node.AVariablesMachineClause;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.POperation;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.PSet;
import de.be4.classicalb.core.parser.node.PSubstitution;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.Token;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.exception.ProBError;
import de.prob.model.representation.Action;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.Constant;
import de.prob.model.representation.Guard;
import de.prob.model.representation.Invariant;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Variable;

public class DomBuilder extends MachineClauseAdapter {

	private final EOF EOF = new EOF();
	private final List<Parameter> parameters = new ArrayList<>();
	private final List<Constraint> constraints = new ArrayList<>();
	private final List<ClassicalBConstant> constants = new ArrayList<>();
	private final List<Property> properties = new ArrayList<>();
	private final List<ClassicalBVariable> variables = new ArrayList<>();
	private final List<ClassicalBInvariant> invariants = new ArrayList<>();
	private final List<de.prob.model.representation.Set> sets = new ArrayList<>();
	private final List<Assertion> assertions = new ArrayList<>();
	private final List<Operation> operations = new ArrayList<>();
	private final Set<String> usedIds = new HashSet<>();
	private final String unprefixedName;
	private final String prefix;

	public DomBuilder(final String unprefixedName, final String prefix) {
		this.unprefixedName = unprefixedName;
		this.prefix = prefix;
	}

	public ClassicalBMachine build(final Start ast) {
		ast.apply(this);
		return getMachine();
	}

	public ClassicalBMachine getMachine() {
		final String prefixedName;
		if (prefix == null || prefix.equals(unprefixedName)) {
			prefixedName = unprefixedName;
		} else {
			prefixedName = prefix + "." + unprefixedName;
		}
		ClassicalBMachine machine = new ClassicalBMachine(prefixedName);
		machine = machine.set(Assertion.class, new ModelElementList<>(assertions));
		machine = machine.set(Constant.class, new ModelElementList<>(constants));
		machine = machine.set(Constraint.class, new ModelElementList<>(constraints));
		machine = machine.set(Property.class, new ModelElementList<>(properties));
		machine = machine.set(Invariant.class, new ModelElementList<>(invariants));
		machine = machine.set(Parameter.class, new ModelElementList<>(parameters));
		machine = machine.set(de.prob.model.representation.Set.class, new ModelElementList<>(sets));
		machine = machine.set(Variable.class, new ModelElementList<>(variables));
		machine = machine.set(BEvent.class, new ModelElementList<>(operations));
		return machine;
	}

	@Override
	public void caseAMachineHeader(final AMachineHeader node) {
		final String nameInAst = extractIdentifierName(node.getName());
		if (!nameInAst.equals(unprefixedName)) {
			throw new ProBError("Machine name mismatch: expected name " + unprefixedName + ", but found name " + nameInAst + " in AST");
		}
		for (PExpression expression : node.getParameters()) {
			parameters.add(new Parameter(createExpressionAST(expression)));
		}
	}

	@Override
	public void caseAConstraintsMachineClause(
			final AConstraintsMachineClause node) {
		List<PPredicate> predicates = getPredicates(node.getPredicates());
		for (PPredicate pPredicate : predicates) {
			constraints.add(new Constraint(createPredicateAST(pPredicate)));
		}
	}

	@Override
	public void caseAConstantsMachineClause(final AConstantsMachineClause node) {
		for (PExpression pExpression : node.getIdentifiers()) {
			constants.add(new ClassicalBConstant(
					createExpressionAST(pExpression)));
		}
	}

	@Override
	public void caseAPropertiesMachineClause(final APropertiesMachineClause node) {
		for (PPredicate pPredicate : getPredicates(node.getPredicates())) {
			properties.add(new Property(createPredicateAST(pPredicate)));
		}
	}

	@Override
	public void caseAVariablesMachineClause(final AVariablesMachineClause node) {
		for (PExpression pExpression : node.getIdentifiers()) {
			if (prefix != null) {
				usedIds.add(extractIdentifierName(pExpression));
			}
			variables.add(new ClassicalBVariable(
					createExpressionAST(pExpression)));
		}
	}

	@Override
	public void caseAInvariantMachineClause(final AInvariantMachineClause node) {
		List<PPredicate> predicates = getPredicates(node.getPredicates());
		for (PPredicate pPredicate : predicates) {
			invariants.add(new ClassicalBInvariant(
					createPredicateAST(pPredicate)));
		}
	}

	@Override
	public void caseASetsMachineClause(final ASetsMachineClause node) {
		for (final PSet set : node.getSetDefinitions()) {
			final List<TIdentifierLiteral> identifier;
			if (set instanceof ADeferredSetSet) {
				identifier = ((ADeferredSetSet)set).getIdentifier();
			} else if (set instanceof AEnumeratedSetSet) {
				identifier = ((AEnumeratedSetSet)set).getIdentifier();
			} else {
				continue;
			}
			sets.add(new de.prob.model.representation.Set(new ClassicalB(
				extractIdentifierName(identifier),
				FormulaExpand.EXPAND
			)));
		}
	}

	@Override
	public void caseAAssertionsMachineClause(final AAssertionsMachineClause node) {
		for (final PPredicate preds : node.getPredicates()) {
			for (PPredicate pPredicate : getPredicates(preds)) {
				assertions.add(new Assertion(createPredicateAST(pPredicate)));
			}
		}
	}

	@Override
	public void caseALocalOperationsMachineClause(final ALocalOperationsMachineClause node) {
		doOperations(node.getOperations());
	}

	@Override
	public void caseAOperationsMachineClause(final AOperationsMachineClause node) {
		doOperations(node.getOperations());
	}

	private void doOperations(final List<? extends POperation> ops) {
		for (final POperation op : ops) {
			if (op instanceof AOperation) {
				doOperation((AOperation)op);
			}
		}
	}

	private void doOperation(final AOperation node) {
		String name = extractIdentifierName(node.getOpName());
		if (prefix != null && !prefix.equals(name)) {
			name = prefix + "." + name;
		}
		List<PExpression> paramIds = node.getParameters();
		final List<String> params = extractIdentifiers(paramIds);
		final List<String> output = extractIdentifiers(node.getReturnValues());
		Operation operation = new Operation(name, params, output);
		PSubstitution body = node.getOperationBody();
		List<ClassicalBGuard> guards = new ArrayList<>();
		if (body instanceof ASelectSubstitution) {
			PPredicate condition = ((ASelectSubstitution) body).getCondition();
			List<PPredicate> predicates = getPredicates(condition);
			for (PPredicate pPredicate : predicates) {
				guards.add(new ClassicalBGuard(createPredicateAST(pPredicate)));
			}
		}
		if (body instanceof APreconditionSubstitution) {
			PPredicate condition = ((APreconditionSubstitution) body)
					.getPredicate();
			List<PPredicate> predicates = getPredicates(condition);
			for (PPredicate pPredicate : predicates) {
				guards.add(new ClassicalBGuard(createPredicateAST(pPredicate)));
			}
		}
		List<ClassicalBAction> actions = new ArrayList<>();
		actions.add(new ClassicalBAction(createSubstitutionAST(body)));
		operation = operation.set(Action.class, new ModelElementList<>(actions));
		operation = operation.set(Guard.class, new ModelElementList<>(guards));

		operations.add(operation);
	}

	// -------------------

	private static String extractIdentifierName(final List<TIdentifierLiteral> nameL) {
		return nameL.stream()
			.map(Token::getText)
			.collect(Collectors.joining("."));
	}
	
	private static String extractIdentifierName(PExpression pExpression) {
		AIdentifierExpression identifierExpression = null;
		if(pExpression instanceof AIdentifierExpression) {
			identifierExpression = (AIdentifierExpression) pExpression;
		} else if(pExpression instanceof ADescriptionExpression) {
			identifierExpression = (AIdentifierExpression) ((ADescriptionExpression) pExpression).getExpression();
		} else {
			throw new ProBError("Invalid expression for extracting identifier name");
		}
		return identifierExpression.getIdentifier().get(0).getText();
	}

	private static List<String> extractIdentifiers(final List<PExpression> identifiers) {
		return identifiers.stream()
			.filter(pExpression -> pExpression instanceof AIdentifierExpression)
			.map(pExpression -> extractIdentifierName(((AIdentifierExpression)pExpression).getIdentifier()))
			.collect(Collectors.toList());
	}

	private static List<PPredicate> getPredicates(final PPredicate node) {
		if (!(node instanceof AConjunctPredicate)) {
			return Collections.singletonList(node);
		}

		final LinkedList<PPredicate> conjunctPreds = new LinkedList<>();
		AConjunctPredicate currentNode = (AConjunctPredicate)node;
		while (currentNode.getLeft() instanceof AConjunctPredicate) {
			conjunctPreds.addFirst(currentNode.getRight());
			currentNode = (AConjunctPredicate)currentNode.getLeft();
		}
		conjunctPreds.addFirst(currentNode.getRight());
		conjunctPreds.addFirst(currentNode.getLeft());

		return conjunctPreds;
	}

	private Start createExpressionAST(final PExpression expression) {
		Start start = new Start();
		AExpressionParseUnit node = new AExpressionParseUnit();
		start.setPParseUnit(node);
		start.setEOF(EOF);
		node.setExpression(expression.clone());
		node.getExpression().apply(new RenameIdentifiers());
		return start;
	}

	private Start createPredicateAST(final PPredicate pPredicate) {
		Start start = new Start();
		APredicateParseUnit node2 = new APredicateParseUnit();
		start.setPParseUnit(node2);
		start.setEOF(EOF);
		node2.setPredicate(pPredicate.clone());
		node2.getPredicate().apply(new RenameIdentifiers());
		return start;
	}

	private Start createSubstitutionAST(final PSubstitution pSub) {
		Start start = new Start();
		ASubstitutionParseUnit node2 = new ASubstitutionParseUnit();
		start.setPParseUnit(node2);
		start.setEOF(EOF);
		node2.setSubstitution(pSub.clone());
		node2.getSubstitution().apply(new RenameIdentifiers());
		return start;
	}

	private class RenameIdentifiers extends DepthFirstAdapter {
		@Override
		public void inAIdentifierExpression(final AIdentifierExpression node) {
			if (prefix != null) {
				String id = node.getIdentifier().get(0).getText();

				if (usedIds.contains(id)) {
					final List<TIdentifierLiteral> prefixTokens = Arrays.stream(prefix.split("\\."))
						.map(TIdentifierLiteral::new)
						.collect(Collectors.toList());
					node.getIdentifier().addAll(0, prefixTokens);
				}
			}
		}
	}
}
