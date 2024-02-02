package de.prob.scripting;

import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.EventB;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.eventb.EventBModel;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.CSPModel;
import de.prob.model.representation.ModelElementList;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

/**
 * Groovy extension module to provide extra methods on ProB objects in a Groovy environment.
 * This is declared in the META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule file in the resources.
 */
public final class ProBGroovyMethods {
	private ProBGroovyMethods() {
		throw new AssertionError("Utility class");
	}

	@SuppressWarnings("unchecked")
	public static <T> T asType(String self, Class<T> clazz) {
		if (clazz == ClassicalB.class) {
			return (T)new ClassicalB(self);
		} else if (clazz == EventB.class) {
			return (T)new EventB(self);
		} else {
			return StringGroovyMethods.asType(self, clazz);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T asType(EvalResult self, Class<T> clazz) {
		if (clazz == Integer.class) {
			return (T)Integer.valueOf(self.getValue());
		} else if (clazz == Double.class) {
			return (T)Double.valueOf(self.getValue());
		} else if (clazz == String.class) {
			return (T)self.getValue();
		} else {
			return DefaultGroovyMethods.asType(self, clazz);
		}
	}

	public static <E> E getAt(ModelElementList<E> self, String name) {
		return self.getElement(name);
	}

	public static AbstractElement getAt(AbstractModel self, String name) {
		return self.getComponent(name);
	}

	/**
	 * This method is implemented to provide access to the {@link State} objects
	 * specified by an integer identifier. This maps to a groovy operator so
	 * that in the console users can type variableOfTypeStateSpace[stateId] and
	 * receive the corresponding State back. An IllegalArgumentException is
	 * thrown if the specified id is unknown.
	 *
	 * @param id ID of the state thate is to be found.
	 * @return {@link State} for the specified ID
	 * @throws IllegalArgumentException if a state with the specified ID doesn't exist
	 */
	public static State getAt(StateSpace self, int id) {
		return self.getState(id);
	}

	/**
	 * This method allows the conversion of the {@link StateSpace} to a Model or a
	 * Trace. This corresponds to the Groovy operator "as". The user convert a
	 * StateSpace to an {@link AbstractModel}, {@link EventBModel},
	 * {@link ClassicalBModel}, or {@link CSPModel}. If they specify the class
	 * {@link Trace}, a new Trace object will be created and returned.
	 *
	 * @param clazz
	 *            the class to convert to
	 * @return the Model or Trace corresponding to the StateSpace instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T asType(StateSpace self, Class<T> clazz) {
		if (clazz == AbstractModel.class || clazz.equals(self.getModel().getClass())) {
			return (T)self.getModel();
		} else if (clazz == Trace.class) {
			return (T)new Trace(self);
		} else {
			return DefaultGroovyMethods.asType(self, clazz);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T asType(Trace self, Class<T> clazz) {
		if (clazz == StateSpace.class) {
			return (T)self.getStateSpace();
		} else if (clazz == AbstractModel.class || clazz == self.getStateSpace().getModel().getClass()) {
			return (T)self.getStateSpace().getModel();
		} else {
			return DefaultGroovyMethods.asType(self, clazz);
		}
	}
}
