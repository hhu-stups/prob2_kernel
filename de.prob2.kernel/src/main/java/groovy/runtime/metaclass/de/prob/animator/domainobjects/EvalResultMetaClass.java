package groovy.runtime.metaclass.de.prob.animator.domainobjects;

import de.prob.animator.domainobjects.EvalResult;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;

public class EvalResultMetaClass extends DelegatingMetaClass {
	public EvalResultMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public EvalResultMetaClass(Class<?> theClass) {
		super(theClass);
	}

	@Override
	public Object getProperty(Object object, String property) {
		try {
			return super.getProperty(object, property);
		} catch (MissingPropertyException e) {
			EvalResult res = (EvalResult)object;
			if (res.getSolutions().containsKey(property)) {
				return res.getSolution(property);
			} else {
				throw e;
			}
		}
	}
}
