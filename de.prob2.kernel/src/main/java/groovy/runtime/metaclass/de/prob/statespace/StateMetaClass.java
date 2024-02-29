package groovy.runtime.metaclass.de.prob.statespace;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;

public class StateMetaClass extends DelegatingMetaClass {
	public StateMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public StateMetaClass(Class<?> theClass) {
		super(theClass);
	}

	@Override
	public Object invokeMethod(Object object, String methodName, Object[] arguments) {
		try {
			return super.invokeMethod(object, methodName, arguments);
		} catch (MissingMethodException e) {
			State state = (State)object;

			if (methodName.startsWith("$") && !Transition.SETUP_CONSTANTS_NAME.equals(methodName) && !Transition.INITIALISE_MACHINE_NAME.equals(methodName)) {
				methodName = methodName.substring(1);
			}

			List<String> preds = Arrays.stream(arguments)
				.map(Object::toString)
				.collect(Collectors.toList());
			return state.perform(methodName, preds);
		}
	}
}
