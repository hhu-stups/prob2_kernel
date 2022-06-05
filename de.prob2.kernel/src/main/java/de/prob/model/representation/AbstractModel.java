package de.prob.model.representation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.DependencyGraph.ERefType;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;
import de.prob.statespace.StateSpace;

import groovy.util.Eval;

public abstract class AbstractModel extends AbstractElement {

	protected final StateSpaceProvider stateSpaceProvider;
	protected File modelFile;
	protected final DependencyGraph graph;

	public AbstractModel(
			StateSpaceProvider stateSpaceProvider,
			Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children,
			DependencyGraph graph, File modelFile) {
		super(children);
		this.stateSpaceProvider = stateSpaceProvider;
		this.graph = graph;
		this.modelFile = modelFile;
	}

	public abstract AbstractElement getComponent(final String name);

	public DependencyGraph getGraph() {
		return graph;
	}

	public ERefType getRelationship(final String comp1, final String comp2) {
		return getEdge(comp1, comp2);
	}

	public ERefType getEdge(final String comp1, final String comp2) {
		final List<ERefType> edges = graph.getRelationships(comp1, comp2);
		if (edges.isEmpty()) {
			return null;
		}
		return edges.get(0);
	}

	@Override
	public String toString() {
		return graph.toString();
	}

	/**
	 * Will parse a formula including information specific to the model at hand.
	 *
	 * @param formula to be parsed
	 * @param expand the expansion behavior to use
	 * @return a valid formula
	 * @throws RuntimeException if parsing is not successful
	 */
	public abstract IEvalElement parseFormula(String formula, FormulaExpand expand);
	
	/**
	 * Will parse a formula including information specific to the model at hand.
	 *
	 * @param formula to be parsed
	 * @return a valid formula
	 * @throws RuntimeException if parsing is not successful
	 * @deprecated Use {@link #parseFormula(String, FormulaExpand)} with an explicit {@link FormulaExpand} argument instead
	 */
	@Deprecated
	public IEvalElement parseFormula(String formula) {
		return this.parseFormula(formula, FormulaExpand.EXPAND);
	}

	/**
	 * <p>Create a formula representing the given identifier.</p>
	 * <p>
	 * Unlike {@link #parseFormula(String, FormulaExpand)},
	 * this method accepts arbitrary strings as identifiers,
	 * even ones that are not syntactically valid in the language of the model
	 * and would be unrepresentable or require quoting.
	 * </p>
	 *
	 * @param identifier list of string parts that make up a dotted identifier
	 * @param expansion expansion mode to use when evaluating the formula
	 * @return a formula representing the given identifier
	 */
	public abstract IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion);

	/**
	 * Will check the syntax of a formula to see if it is valid in the scope of
	 * this model.
	 *
	 * @param formula to be checked
	 * @return whether or not the formula in question has valid syntax in the
	 *         scope of this model
	 */
	public boolean checkSyntax(final String formula) {
		try {
			parseFormula(formula, FormulaExpand.TRUNCATE);
			return true;
		} catch (EvaluationException ignored) {
			return false;
		}
	}

	public abstract FormalismType getFormalismType();

	public abstract Language getLanguage();

	public File getModelFile() {
		return modelFile;
	}

	public StateSpaceProvider getStateSpaceProvider() {
		return stateSpaceProvider;
	}

	/**
	 * @deprecated This method is unsafe and can execute arbitrary Groovy code, depending on the argument.
	 *     To look up a component by name, use {@link #getComponent(String)} instead.
	 *     To look up sub-elements of a component, use {@link #getChildrenOfType(Class)} and {@link ModelElementList#getElement(String)}.
	 */
	@Deprecated
	public AbstractElement get(List<String> path) {
		if (path.isEmpty()) {
			return null;
		}
		return (AbstractElement) Eval.x(this, "x." + String.join(".", path));
	}

	public abstract AbstractCommand getLoadCommand(final AbstractElement mainComponent);

	public void loadIntoStateSpace(final StateSpace stateSpace, final AbstractElement mainComponent) {
		StateSpaceProvider.loadFromCommandIntoStateSpace(stateSpace, this, mainComponent, this.getLoadCommand(mainComponent));
	}

	public StateSpace load(AbstractElement mainComponent) {
		return load(mainComponent, new HashMap<>());
	}

	public StateSpace load(final AbstractElement mainComponent, final Map<String, String> preferences) {
		final StateSpace stateSpace = getStateSpaceProvider().getStateSpace();
		try {
			stateSpace.changePreferences(preferences);
			this.loadIntoStateSpace(stateSpace, mainComponent);
			return stateSpace;
		} catch (RuntimeException e) {
			stateSpace.kill();
			throw e;
		}
	}
}
