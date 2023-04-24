package de.prob.model.representation;

import java.util.Map;
import java.util.Set;

import com.github.krukow.clj_lang.PersistentHashMap;

import groovy.lang.GroovyObjectSupport;

/**
 * This class is the subclass of all model elements (Models, Machines, Contexts,
 * Variables, etc.)
 *
 * @author joy
 *
 */
public abstract class AbstractElement extends GroovyObjectSupport {

	/**
	 * Maps from a subclass of {@link AbstractElement} to a set containing all
	 * elements for that subclass
	 */
	private final PersistentHashMap<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children;

	public AbstractElement() {
		this(PersistentHashMap.emptyMap());
	}

	public AbstractElement(Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children) {
		if (children instanceof PersistentHashMap<?, ?>) {
			// Avoid copying already created PersistentHashMap if possible.
			this.children = (PersistentHashMap<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>>)children;
		} else {
			// If the map has a different type, copy/convert it to PersistentHashMap.
			this.children = PersistentHashMap.create(children);
		}
	}

	/**
	 * Each {@link AbstractElement} can have children of a subclass that extends
	 * {@link AbstractElement}. These are specified by the class of the child.
	 * To get a Set of all of the children of a particular class, use this
	 * method.
	 *
	 * @param c
	 *            {@link Class} T of the desired type of children
	 * @param <T> the desired type of children
	 * @return {@link Set} containing all the children of type T
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractElement> ModelElementList<T> getChildrenOfType(
			final Class<T> c) {
		ModelElementList<? extends AbstractElement> list = getChildren().get(c);
		if (list == null) {
			return new ModelElementList<>();
		}
		return (ModelElementList<T>) list;
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractElement, S extends T> ModelElementList<S> getChildrenAndCast(Class<T> key, Class<S> realType) {
		ModelElementList<? extends AbstractElement> list = getChildren().get(key);
		if (list == null) {
			return new ModelElementList<>();
		}
		return (ModelElementList<S>) list;
	}

	protected Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>>assoc(
			Class<? extends AbstractElement> key, ModelElementList<? extends AbstractElement> val) {
		return (PersistentHashMap<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>>) children.assoc(key, val);
	}

	/**
	 * @return the {@link Map} of {@link Class} to {@link Set} of children
	 */
	public Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> getChildren() {
		return children;
	}
}
