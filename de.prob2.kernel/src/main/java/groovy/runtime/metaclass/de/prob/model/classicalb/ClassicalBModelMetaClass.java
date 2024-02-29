package groovy.runtime.metaclass.de.prob.model.classicalb;

import groovy.lang.MetaClass;
import groovy.runtime.metaclass.de.prob.model.representation.AbstractModelMetaClass;

public class ClassicalBModelMetaClass extends AbstractModelMetaClass {
	public ClassicalBModelMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public ClassicalBModelMetaClass(Class<?> theClass) {
		super(theClass);
	}
}
