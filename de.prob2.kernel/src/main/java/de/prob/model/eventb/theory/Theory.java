package de.prob.model.eventb.theory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.prob.model.eventb.EventBAxiom;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Named;
import de.prob.tmparser.OperatorMapping;

import org.eventb.core.ast.extension.IFormulaExtension;

public class Theory extends AbstractElement implements Named {

	private final String name;
	private final String parentDirectory;
	private final Collection<OperatorMapping> proBMappings;
	private Set<IFormulaExtension> typeEnvironment;

	public Theory(final String name, final String parentDirectory,
			final Collection<OperatorMapping> mappings) {
		this(
				name,
				parentDirectory,
				mappings,
				Collections.<IFormulaExtension> emptySet(),
				Collections.emptyMap());
	}

	private Theory(
			final String name,
			final String parentDirectory,
			final Collection<OperatorMapping> proBMappings,
			Set<IFormulaExtension> typeEnvironment,
			Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(children);
		this.name = name;
		this.parentDirectory = parentDirectory;
		this.proBMappings = proBMappings;
		this.typeEnvironment = typeEnvironment;
	}

	/**
	 * @deprecated Use the strongly typed {@code with...} methods instead.
	 */
	@Deprecated
	public Theory set(Class<? extends AbstractElement> clazz,
			ModelElementList<? extends AbstractElement> elements) {
		return new Theory(name, parentDirectory, proBMappings, typeEnvironment,
				assoc(clazz, elements));
	}

	private <T extends AbstractElement> Theory withChildrenOfType(final Class<T> clazz, final ModelElementList<? extends T> elements) {
		return new Theory(name, parentDirectory, proBMappings, typeEnvironment, assoc(clazz, elements));
	}

	public ModelElementList<DataType> getDataTypes() {
		return getChildrenOfType(DataType.class);
	}

	public Theory withDataTypes(final ModelElementList<DataType> dataTypes) {
		return this.withChildrenOfType(DataType.class, dataTypes);
	}

	public ModelElementList<Theory> getImported() {
		return getChildrenOfType(Theory.class);
	}

	public Theory withImported(final ModelElementList<Theory> imported) {
		return this.withChildrenOfType(Theory.class, imported);
	}

	public ModelElementList<Operator> getOperators() {
		return getChildrenOfType(Operator.class);
	}

	public Theory withOperators(final ModelElementList<Operator> operators) {
		return this.withChildrenOfType(Operator.class, operators);
	}

	public ModelElementList<AxiomaticDefinitionBlock> getAxiomaticDefinitionBlocks() {
		return getChildrenOfType(AxiomaticDefinitionBlock.class);
	}

	public Theory withAxiomaticDefinitionBlocks(final ModelElementList<AxiomaticDefinitionBlock> axiomaticDefinitionBlocks) {
		return this.withChildrenOfType(AxiomaticDefinitionBlock.class, axiomaticDefinitionBlocks);
	}

	public ModelElementList<ProofRulesBlock> getProofRules() {
		return getChildrenOfType(ProofRulesBlock.class);
	}

	public Theory withProofRules(final ModelElementList<ProofRulesBlock> proofRules) {
		return this.withChildrenOfType(ProofRulesBlock.class, proofRules);
	}

	public ModelElementList<EventBAxiom> getTheorems() {
		return getChildrenOfType(EventBAxiom.class);
	}

	public Theory withTheorems(final ModelElementList<EventBAxiom> theorems) {
		return this.withChildrenOfType(EventBAxiom.class, theorems);
	}

	public ModelElementList<Type> getTypeParameters() {
		return getChildrenOfType(Type.class);
	}

	public Theory withTypeParameters(final ModelElementList<Type> typeParameters) {
		return this.withChildrenOfType(Type.class, typeParameters);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getParentDirectoryName() {
		return parentDirectory;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final Theory other = (Theory)obj;
		return Objects.equals(this.getParentDirectoryName(), other.getParentDirectoryName())
				&& Objects.equals(this.getName(), other.getName());
	}

	public Collection<OperatorMapping> getProBMappings() {
		return proBMappings;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getParentDirectoryName(), this.getName());
	}

	public Theory setTypeEnvironment(Set<IFormulaExtension> typeEnvironment) {
		return new Theory(name, parentDirectory, proBMappings, typeEnvironment, getChildren());
	}

	public Set<IFormulaExtension> getTypeEnvironment() {
		return typeEnvironment;
	}
}
