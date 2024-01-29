package de.prob.model.eventb.translate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.prob.animator.domainobjects.EventB;
import de.prob.model.eventb.EventBAxiom;
import de.prob.model.eventb.theory.AxiomaticDefinitionBlock;
import de.prob.model.eventb.theory.DataType;
import de.prob.model.eventb.theory.DataTypeConstructor;
import de.prob.model.eventb.theory.DataTypeDestructor;
import de.prob.model.eventb.theory.DirectDefinition;
import de.prob.model.eventb.theory.IOperatorDefinition;
import de.prob.model.eventb.theory.Operator;
import de.prob.model.eventb.theory.OperatorArgument;
import de.prob.model.eventb.theory.RecursiveDefinitionCase;
import de.prob.model.eventb.theory.RecursiveOperatorDefinition;
import de.prob.model.eventb.theory.Theory;
import de.prob.model.eventb.theory.Type;
import de.prob.model.representation.ModelElementList;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.tmparser.OperatorMapping;

import org.eventb.core.ast.Expression;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.extension.IFormulaExtension;

public class TheoryTranslator {

	private final List<Theory> theories;
	private Set<IFormulaExtension> typeEnv;

	public TheoryTranslator(final ModelElementList<Theory> theories) {
		this.theories = new ArrayList<>();

		for (Theory theory : theories) {
			if (!this.theories.contains(theory)) {
				for (Theory t : theory.getImported()) {
					if (!this.theories.contains(t)) {
						this.theories.add(t);
					}
				}
				this.theories.add(theory);
			}
		}
	}

	public void toProlog(final IPrologTermOutput pto) {
		for (Theory theory : theories) {
			typeEnv = theory.getTypeEnvironment();
			pto.openTerm("theory");
			printTheoryName(theory, pto);
			printListOfImportedTheories(theory.getImported(), pto);
			printTypeParameters(theory.getTypeParameters(), pto);
			printDataTypes(theory.getDataTypes(), pto);
			printOperatorDefs(theory.getOperators(), pto);
			printAxiomaticDefinitionBlocks(
					theory.getAxiomaticDefinitionBlocks(), pto);
			printMappings(theory.getProBMappings(), pto);
			pto.closeTerm();
		}
	}

	public void printTheoryName(final Theory t, final IPrologTermOutput pto) {
		pto.openTerm("theory_name");
		pto.printAtom(t.getParentDirectoryName());
		pto.printAtom(t.getName());
		pto.closeTerm();
	}

	private void printListOfImportedTheories(
			final ModelElementList<Theory> imported, final IPrologTermOutput pto) {
		pto.openList();
		for (Theory theory : imported) {
			printTheoryName(theory, pto);
		}
		pto.closeList();
	}

	private void printTypeParameters(
			final ModelElementList<Type> typeParameters,
			final IPrologTermOutput pto) {
		pto.openList();
		for (Type t : typeParameters) {
			pto.printAtom(t.toString());
		}
		pto.closeList();
	}

	private void printDataTypes(final ModelElementList<DataType> dataTypes,
			final IPrologTermOutput pto) {
		pto.openList();
		for (DataType dataType : dataTypes) {
			printDataType(dataType, dataTypes, pto);
		}
		pto.closeList();
	}

	private void printDataType(final DataType dataType, final ModelElementList<DataType> allDataTypes,
			final IPrologTermOutput pto) {
		pto.openTerm("datatype");
		pto.printAtom(dataType.toString());
		//System.out.println("Datatype: " + dataType.toString());
		pto.openList();
		for (String arg : dataType.getTypeArguments()) {
			printType(arg, pto);
		}
		pto.closeList();
		pto.openList();
		for (final DataTypeConstructor constructor : dataType.getConstructorsByName().values()) {
			printConstructor(constructor, allDataTypes, pto);
		}
		pto.closeList();
		pto.closeTerm();
	}

	private void printType(final String type, final IPrologTermOutput pto) {
		new EventB(type, typeEnv).printProlog(pto);
	}

	private void printConstructor(final DataTypeConstructor constructor, final ModelElementList<DataType> allDataTypes,
			final IPrologTermOutput pto) {
		pto.openTerm("constructor");
		pto.printAtom(constructor.getName());
		//System.out.println("Constructor " + constructor.getName());
		//System.out.println("Type env "+typeEnv);
		pto.openList();
		for (final DataTypeDestructor arg : constructor.getArguments()) {
			printTypedDestructorIdentifier("destructor", arg.getName(), new EventB(arg.getType(), typeEnv), allDataTypes, pto);
		}
		pto.closeList();
		pto.closeTerm();
	}

	private void printTypedIdentifier(final String functor,
			final String idString, final EventB type,
			final IPrologTermOutput pto) {
		pto.openTerm(functor);
		pto.printAtom(idString);
		type.printProlog(pto); // TODO: use printPrologExpr() to avoid parsing as Predicate/Subst.
		pto.closeTerm();
	}

	// print in the context of a dataType definition, detecting recursive references to the dataType name
	// TODO: we need a set of current datatypes, as we can also refer other previously defined datatypes (not just the current one)
	private void printTypedDestructorIdentifier(final String functor,
			final String idString, final EventB type, final ModelElementList<DataType> allDataTypes,
			final IPrologTermOutput pto) {
		System.out.println("Typed id "+ functor + " id " + idString + " :: " + type);
		pto.openTerm(functor);
		pto.printAtom(idString);
		DataType dataType = null;
		// I have no idea why this does not work: allDataTypes.getElement(type.toString());
		// so we check by iteration whether the datatype is in the list of datatypes currently being added:
		for (DataType dt : allDataTypes) {
			if (dt.toString().equals(type.toString())) {
				dataType = dt;
				break;
			}
		}
		if (dataType != null) {
			System.out.println("recursive reference to "+ type.toString());
			// we need to generate something like extended_expr(none,'MyList',[identifier(none,'T')],[]))
			pto.openTerm("extended_expr");
			pto.printAtom("none");
			pto.printAtom(dataType.toString());
			pto.openList();
			for (String typearg : dataType.getTypeArguments()) {
				pto.openTerm("identifier");
				pto.printAtom("none");
				pto.printAtom(typearg);
				pto.closeTerm();
			}
			pto.closeList();
			pto.openList();
			pto.closeList();
			pto.closeTerm();
		} else {
			type.printProlog(pto); // TODO ??: use printPrologExpr() to avoid parsing as Predicate/Subst.
		}
		pto.closeTerm();
	}

	private void printOperatorDefs(final ModelElementList<Operator> operators,
			final IPrologTermOutput pto) {
		pto.openList();
		for (Operator operator : operators) {
			printOperator(operator, pto);
		}
		pto.closeList();
	}

	private void printOperator(final Operator operator,
			final IPrologTermOutput pto) {
		pto.openTerm("operator");
		pto.printAtom(operator.toString());

		printOperatorArguments(operator.getArguments(), pto);
		
		// operator.getWD().printProlog(pto); // old version; this contains only the WD conditions inside the direct definition body
		operator.getPredicate().printProlog(pto); // this seems to be the real user-defined WD condition
		// TODO: do we want to export both and extend the Prolog format?
		// The old getWD condition is implicit in the definition body and not that useful for ProB?
		
		processDefinition(operator.getDefinition(), pto);

		pto.closeTerm();
	}

	private void processDefinition(final IOperatorDefinition definition,
			final IPrologTermOutput pto) {
		if (definition instanceof DirectDefinition) {
			printDirectDefinition((DirectDefinition) definition, pto);

			// Empty list for recursive definitions
			pto.openList();
			pto.closeList();
		}
		if (definition instanceof RecursiveOperatorDefinition) {
			// Empty list for direct definitions
			pto.openList();
			pto.closeList();

			printRecursiveDefinition((RecursiveOperatorDefinition) definition,
					pto);
		}
	}

	private void printRecursiveDefinition(
			final RecursiveOperatorDefinition definition,
			final IPrologTermOutput pto) {
		EventB inductiveArgument = definition.getInductiveArgument();
		pto.openList();
		ModelElementList<RecursiveDefinitionCase> cases = definition.getCases();
		for (RecursiveDefinitionCase c : cases) {
			printRecursiveCase(inductiveArgument, c, pto);
		}
		pto.closeList();
	}

	private void printRecursiveCase(final EventB inductiveArgument,
			final RecursiveDefinitionCase c, final IPrologTermOutput pto) {
		pto.openTerm("case");
		pto.printAtom(inductiveArgument.getCode());
		pto.openList();
		Expression expression = c.getExpression().getRodinParsedResult()
				.getParsedExpression();
		for (FreeIdentifier fi : expression.getFreeIdentifiers()) {
			pto.printAtom(fi.getName());
		}
		pto.closeList();
		c.getExpression().printProlog(pto);
		c.getFormula().printProlog(pto);
		pto.closeTerm();
	}

	private void printDirectDefinition(final DirectDefinition definition,
			final IPrologTermOutput pto) {
		pto.openList();
		definition.getFormula().printProlog(pto);
		pto.closeList();
	}

	private void printOperatorArguments(final List<OperatorArgument> arguments,
			final IPrologTermOutput pto) {
		pto.openList();
		for (OperatorArgument argument : arguments) {
			printTypedIdentifier("argument", argument.getIdentifier()
					.toString(), argument.getType(), pto);
		}
		pto.closeList();
	}

	private void printAxiomaticDefinitionBlocks(
			final ModelElementList<AxiomaticDefinitionBlock> axiomaticDefinitionBlocks,
			final IPrologTermOutput pto) {
		pto.openList();
		for (AxiomaticDefinitionBlock block : axiomaticDefinitionBlocks) {
			printAxiomaticDefinitonBlock(block, pto);
		}
		pto.closeList();
	}

	private void printAxiomaticDefinitonBlock(
			final AxiomaticDefinitionBlock block, final IPrologTermOutput pto) {
		pto.openTerm("axiomatic_def_block");
		pto.printAtom(block.getName());

		printTypeParameters(block.getTypeParameters(), pto);

		pto.openList();
		for (Operator operator : block.getOperators()) {
			printAxiomaticOperator(operator, pto);
		}
		pto.closeList();

		pto.openList();
		for (EventBAxiom axiom : block.getAxioms()) {
			axiom.getPredicate().printProlog(pto);
		}
		pto.closeList();

		pto.closeTerm();
	}

	private void printAxiomaticOperator(final Operator operator,
			final IPrologTermOutput pto) {
		pto.openTerm("opdef");
		pto.printAtom(operator.toString());
		printOperatorArguments(operator.getArguments(), pto);
		pto.openList();
		operator.getWD().printProlog(pto);
		pto.closeList();
		pto.closeTerm();
	}

	private void printMappings(final Collection<OperatorMapping> proBMappings,
			final IPrologTermOutput pto) {
		pto.openList();
		for (OperatorMapping mapping : proBMappings) {
			mapping.printProlog(pto);
		}
		pto.closeList();
	}

}
