package de.prob.model.classicalb;

import java.util.Collections;
import java.util.Map;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.BEvent;
import de.prob.model.representation.Constant;
import de.prob.model.representation.ConstantsComponent;
import de.prob.model.representation.Invariant;
import de.prob.model.representation.Machine;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Set;
import de.prob.model.representation.Variable;

public class ClassicalBMachine extends Machine implements ConstantsComponent {
	public ClassicalBMachine(final String name) {
		this(name, Collections.emptyMap());
	}

	private ClassicalBMachine(final String name, final Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		super(name, children);
	}

	public ClassicalBMachine addTo(final AbstractElement element) {
		final ModelElementList<AbstractElement> childrenList = getChildrenOfType(AbstractElement.class);
		return new ClassicalBMachine(getName(), assoc(AbstractElement.class, childrenList.addElement(element)));
	}

	public ClassicalBMachine set(final Class<? extends AbstractElement> clazz, final ModelElementList<? extends AbstractElement> elements) {
		return new ClassicalBMachine(getName(), assoc(clazz, elements));
	}

	public ModelElementList<Parameter> getParameters() {
		return getChildrenOfType(Parameter.class);
	}

	@Override
	public ModelElementList<Set> getSets() {
		return getChildrenOfType(Set.class);
	}

	public ModelElementList<Constraint> getConstraints() {
		return getChildrenOfType(Constraint.class);
	}

	@Override
	public ModelElementList<ClassicalBConstant> getConstants() {
		return getChildrenAndCast(Constant.class, ClassicalBConstant.class);
	}

	public ModelElementList<Property> getProperties() {
		return getChildrenOfType(Property.class);
	}

	@Override
	public ModelElementList<Property> getAxioms() {
		return this.getProperties();
	}

	@Override
	public ModelElementList<ClassicalBVariable> getVariables() {
		return getChildrenAndCast(Variable.class, ClassicalBVariable.class);
	}

	@Override
	public ModelElementList<ClassicalBInvariant> getInvariants() {
		return getChildrenAndCast(Invariant.class, ClassicalBInvariant.class);
	}

	public ModelElementList<Assertion> getAssertions() {
		return getChildrenOfType(Assertion.class);
	}

	public ModelElementList<Operation> getOperations() {
		return getChildrenAndCast(BEvent.class, Operation.class);
	}

	@Override
	public ModelElementList<Operation> getEvents() {
		return getChildrenAndCast(BEvent.class, Operation.class);
	}

	public Operation getOperation(String name) {
		return getOperations().getElement(name);
	}

	@Override
	public Operation getEvent(final String eventName) {
		return this.getOperation(eventName);
	}
}
