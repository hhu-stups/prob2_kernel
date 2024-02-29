package groovy.runtime.metaclass.de.prob.statespace;

import de.prob.statespace.SyncedTraces;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;

public class SyncedTracesMetaClass extends DelegatingMetaClass {
	public SyncedTracesMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public SyncedTracesMetaClass(Class<?> theClass) {
		super(theClass);
	}

	@Override
	public Object invokeMethod(Object object, String methodName, Object[] arguments) {
		try {
			return super.invokeMethod(object, methodName, arguments);
		} catch (MissingMethodException e) {
			SyncedTraces traces = (SyncedTraces)object;
			return traces.execute(methodName);
		}
	}
}
