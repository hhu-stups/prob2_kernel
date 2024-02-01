package de.prob.scripting;

import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.EventB;
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
}
