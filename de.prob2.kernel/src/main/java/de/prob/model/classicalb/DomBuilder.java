package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.exception.ProBError;
import de.prob.model.representation.*;

import java.io.File;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class DomBuilder extends MachineClauseAdapter {

	private final EOF EOF = new EOF();
	private final List<Parameter> parameters = new ArrayList<>();
	private final List<Constraint> constraints = new ArrayList<>();
	private final List<ClassicalBConstant> constants = new ArrayList<>();
	private final List<Property> properties = new ArrayList<>();
	private final List<ClassicalBVariable> variables = new ArrayList<>();
	private final List<ClassicalBInvariant> invariants = new ArrayList<>();
	private final List<de.prob.model.representation.Set> sets = new ArrayList<>();
	private final List<Freetype> freetypes = new ArrayList<>();
	private final List<Assertion> assertions = new ArrayList<>();
	private final List<Operation> operations = new ArrayList<>();
	private final Set<String> usedIds = new HashSet<>();
	private final File modelFile;
	private final String unprefixedName;
	private final String prefix;

	DomBuilder(final File modelFile, final String unprefixedName, final String prefix) {
		this.modelFile = modelFile;
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
		machine = machine.set(Freetype.class, new ModelElementList<>(freetypes));
		machine = machine.set(Variable.class, new ModelElementList<>(variables));
		machine = machine.set(BEvent.class, new ModelElementList<>(operations));
		return machine;
	}

	@Override
	public void caseAMachineHeader(final AMachineHeader node) {
		final String nameInAst = Utils.getTIdentifierListAsString(node.getName());
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
			constraints.add(new Constraint(createPredicateAST(pPredicate), getPragmaLabelName(pPredicate)));
		}
	}

	@Override
	public void caseAConstantsMachineClause(final AConstantsMachineClause node) {
		for (PExpression pExpression : node.getIdentifiers()) {
			AIdentifierExpression idExpr = extractIdentifierExpression(pExpression);
			constants.add(new ClassicalBConstant(createExpressionAST(idExpr)));
		}
	}

	@Override
	public void caseAPropertiesMachineClause(final APropertiesMachineClause node) {
		for (PPredicate pPredicate : getPredicates(node.getPredicates())) {
			properties.add(new Property(createPredicateAST(pPredicate), getPragmaLabelName(pPredicate)));
		}
	}

	@Override
	public void caseAVariablesMachineClause(final AVariablesMachineClause node) {
		for (PExpression pExpression : node.getIdentifiers()) {
			AIdentifierExpression idExpr = extractIdentifierExpression(pExpression);
			if (prefix != null) {
				usedIds.add(Utils.getAIdentifierAsString(idExpr));
			}
			variables.add(new ClassicalBVariable(createExpressionAST(idExpr)));
		}
	}

	@Override
	public void caseAInvariantMachineClause(final AInvariantMachineClause node) {
		List<PPredicate> predicates = getPredicates(node.getPredicates());
		for (PPredicate pPredicate : predicates) {
			invariants.add(new ClassicalBInvariant(createPredicateAST(pPredicate), getPragmaLabelName(pPredicate)));
		}
	}

	@Override
	public void caseASetsMachineClause(final ASetsMachineClause node) {
		for (final PSet set : node.getSetDefinitions()) {
			List<TIdentifierLiteral> identifier;
			if (set instanceof ADeferredSetSet) {
				identifier = ((ADeferredSetSet) set).getIdentifier();
			} else if (set instanceof AEnumeratedSetSet) {
				identifier = ((AEnumeratedSetSet) set).getIdentifier();
			} else {
				continue;
			}
			// Need to clone all tokens so that createExpressionAST doesn't modify the original AST...
			identifier = identifier.stream()
				.map(TIdentifierLiteral::clone)
				.collect(Collectors.toList());
			sets.add(new de.prob.model.representation.Set(new ClassicalB(createExpressionAST(new AIdentifierExpression(identifier)))));
		}
	}

	@Override
	public void caseAFreetypesMachineClause(AFreetypesMachineClause node) {
		for (PFreetype pFreetype : node.getFreetypes()) {
			if (pFreetype instanceof AFreetype) {
				AFreetype ft = (AFreetype) pFreetype;
				// TODO: is this prefixing required?
				String name = addPrefix(ft.getName().getText());
				List<String> parameters = extractIdentifiers(ft.getParameters());
				Freetype freetype = new Freetype(name, parameters);

				List<FreetypeConstructor> constructors = new ArrayList<>();
				for (PFreetypeConstructor c : ft.getConstructors()) {
					if (c instanceof AConstructorFreetypeConstructor) {
						AConstructorFreetypeConstructor cc = (AConstructorFreetypeConstructor) c;
						constructors.add(new FreetypeConstructor(cc.getName().getText(), createExpressionAST(cc.getArgument())));
					} else if (c instanceof AElementFreetypeConstructor) {
						AElementFreetypeConstructor ec = (AElementFreetypeConstructor) c;
						constructors.add(new FreetypeConstructor(ec.getName().getText()));
					}
				}
				freetype = freetype.set(FreetypeConstructor.class, new ModelElementList<>(constructors));

				this.freetypes.add(freetype);
			}
		}
	}

	@Override
	public void caseAAssertionsMachineClause(final AAssertionsMachineClause node) {
		for (final PPredicate pPredicate : node.getPredicates()) {
			// we used to add each conjunct of pPredicate individually, but this will flatten the list of assertions
			assertions.add(new Assertion(createPredicateAST(pPredicate), getPragmaLabelName(pPredicate)));
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
				doOperation((AOperation) op);
			}
		}
	}

	private String addPrefix(String name) {
		if (prefix != null && !prefix.equals(name)) {
			return prefix + "." + name;
		} else {
			return name;
		}
	}

	private void doOperation(final AOperation node) {
		String name = addPrefix(Utils.getTIdentifierListAsString(node.getOpName()));
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
				guards.add(new ClassicalBGuard(createPredicateAST(pPredicate), getPragmaLabelName(pPredicate)));
			}
		}
		if (body instanceof APreconditionSubstitution) {
			PPredicate condition = ((APreconditionSubstitution) body).getPredicate();
			List<PPredicate> predicates = getPredicates(condition);
			for (PPredicate pPredicate : predicates) {
				guards.add(new ClassicalBGuard(createPredicateAST(pPredicate), getPragmaLabelName(pPredicate)));
			}
		}
		List<ClassicalBAction> actions = new ArrayList<>();
		actions.add(new ClassicalBAction(createSubstitutionAST(body)));
		operation = operation.set(Action.class, new ModelElementList<>(actions));
		operation = operation.set(Guard.class, new ModelElementList<>(guards));

		operations.add(operation);
	}

	private AIdentifierExpression extractIdentifierExpression(PExpression pExpression) {
		if (pExpression instanceof AIdentifierExpression) {
			return (AIdentifierExpression) pExpression;
		} else if (pExpression instanceof ADescriptionExpression) {
			return (AIdentifierExpression) ((ADescriptionExpression) pExpression).getExpression();
		} else if (pExpression instanceof ADefinitionExpression) {
			// TODO Perhaps we should add better helper methods for converting positions like this?
			BException.Location nodeLocation = BException.Location.fromNode(this.modelFile.toString(), pExpression);
			List<ErrorItem.Location> locations;
			if (nodeLocation == null) {
				locations = Collections.emptyList();
			} else {
				locations = Collections.singletonList(ErrorItem.Location.fromParserLocation(nodeLocation));
			}
			ErrorItem error = new ErrorItem("ProB2 does not yet support DEFINITIONS for identifier lists: " + pExpression, ErrorItem.Type.ERROR, locations);
			throw new ProBError(Collections.singletonList(error));
			// TODO: analyse body of expression
		} else {
			throw new ProBError("Not a valid constant/variable identifier expression: " + pExpression.getClass());
		}
	}

	private List<String> extractIdentifiers(final List<PExpression> identifiers) {
		return identifiers.stream()
			.map(this::extractIdentifierExpression)
			.map(Utils::getAIdentifierAsString)
			.collect(Collectors.toList());
	}

	private static List<PPredicate> getPredicates(final PPredicate node) {
		if (!(node instanceof AConjunctPredicate)) {
			return Collections.singletonList(node);
		}

		final LinkedList<PPredicate> conjunctPreds = new LinkedList<>();
		AConjunctPredicate currentNode = (AConjunctPredicate) node;
		while (currentNode.getLeft() instanceof AConjunctPredicate) {
			conjunctPreds.addFirst(currentNode.getRight());
			currentNode = (AConjunctPredicate) currentNode.getLeft();
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

	private String getPragmaLabelName(final PPredicate pPredicate) {
		if (pPredicate instanceof ALabelPredicate) {
			String text = ((ALabelPredicate) pPredicate).getName().getText();
			return Utils.unquotePragmaIdentifier(text);
		}

		return null;
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
				String id = Utils.getAIdentifierAsString(node);

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
