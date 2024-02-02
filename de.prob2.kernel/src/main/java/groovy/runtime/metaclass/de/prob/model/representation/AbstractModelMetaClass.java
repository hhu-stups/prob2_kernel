package groovy.runtime.metaclass.de.prob.model.representation;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;

public class AbstractModelMetaClass extends DelegatingMetaClass {
	public AbstractModelMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public AbstractModelMetaClass(Class<?> theClass) {
		super(theClass);
	}

	@Override
	public Object getProperty(Object object, String property) {
		AbstractModel model = (AbstractModel)object;
		AbstractElement component = model.getComponent(property);
		if (component != null) {
			return component;
		} else {
			return super.getProperty(object, property);
		}
	}
}
