package groovy.runtime.metaclass.de.prob.model.representation;

import de.prob.model.representation.ModelElementList;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;

public class ModelElementListMetaClass extends DelegatingMetaClass {
	public ModelElementListMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public ModelElementListMetaClass(Class<?> theClass) {
		super(theClass);
	}

	@Override
	public Object getProperty(Object object, String property) {
		ModelElementList<?> list = (ModelElementList<?>)object;
		return list.getElement(property);
	}
}
