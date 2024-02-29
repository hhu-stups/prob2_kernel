package groovy.runtime.metaclass.de.prob.animator.domainobjects;

import de.prob.animator.domainobjects.TranslatedEvalResult;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;

public class TranslatedEvalResultMetaClass extends DelegatingMetaClass {
	public TranslatedEvalResultMetaClass(MetaClass delegate) {
		super(delegate);
	}

	public TranslatedEvalResultMetaClass(Class<?> theClass) {
		super(theClass);
	}

	@Override
	public Object getProperty(Object object, String property) {
		try {
			return super.getProperty(object, property);
		} catch (MissingPropertyException e) {
			TranslatedEvalResult<?> res = (TranslatedEvalResult<?>)object;
			if (res.getSolutions().containsKey(property)) {
				return res.getSolution(property);
			} else {
				throw e;
			}
		}
	}
}
