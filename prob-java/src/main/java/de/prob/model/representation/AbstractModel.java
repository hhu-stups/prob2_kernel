package de.prob.model.representation;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.StartAnimationCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.eventb.EventBModel;
import de.prob.model.representation.DependencyGraph.ERefType;
import de.prob.scripting.ExtractedModel;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;
import de.prob.statespace.StateSpace;

public abstract class AbstractModel extends AbstractElement {
	// The stateSpaceProvider field will be removed in the future,
	// but isn't deprecated yet,
	// because the subclasses still need it to support the old API.
	protected final StateSpaceProvider stateSpaceProvider;
	protected final DependencyGraph graph;
	protected final File modelFile;

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

	public abstract AbstractElement getMainComponent();

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
	 * Get a list of all files from which this model was loaded.
	 * This always includes the main model file ({@link #getModelFile()}),
	 * and possibly other files referenced from the main file (directly or indirectly).
	 * 
	 * @return list of all files that make up this model
	 */
	public List<Path> getAllFiles() {
		if (this.getModelFile() == null) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(this.getModelFile().toPath());
		}
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
	 * Will parse a classical B formula without respecting the model's language.
	 * <br>
	 * This API is still in beta, do not depend on it!
	 *
	 * @param formula to be parsed, must be parsable as classical B
	 * @param expand  the expansion behavior to use
	 * @return a valid formula
	 * @throws RuntimeException if parsing is not successful
	 */
	public ClassicalB parseFormulaAsClassicalB(String formula, FormulaExpand expand) {
		return new ClassicalB(formula, expand);
	}
	
	/**
	 * Will parse a formula including information specific to the model at hand.
	 *
	 * @param formula to be parsed
	 * @return a valid formula
	 * @throws RuntimeException if parsing is not successful
	 */
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
	 * <p>Create a formula representing the given identifier.</p>
	 * <p>
	 * Unlike {@link #parseFormula(String)},
	 * this method accepts arbitrary strings as identifiers,
	 * even ones that are not syntactically valid in the language of the model
	 * and would be unrepresentable or require quoting.
	 * </p>
	 *
	 * @param identifier list of string parts that make up a dotted identifier
	 * @return a formula representing the given identifier
	 */
	public IEvalElement formulaFromIdentifier(final List<String> identifier) {
		return this.formulaFromIdentifier(identifier, FormulaExpand.TRUNCATE);
	}

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
			parseFormula(formula);
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

	@Deprecated
	public StateSpaceProvider getStateSpaceProvider() {
		return stateSpaceProvider;
	}

	public abstract AbstractCommand getLoadCommand();

	/**
	 * @deprecated Use {@link #getLoadCommand()} instead.
	 * @param mainComponent this model's main component, as returned by {@link ExtractedModel#getMainComponent()}
	 * @return the command for loading this model into the animator
	 */
	@Deprecated
	public AbstractCommand getLoadCommand(AbstractElement mainComponent) {
		return this.getLoadCommand();
	}

	public void loadIntoStateSpace(final StateSpace stateSpace) {
		stateSpace.initModel(this);
		stateSpace.execute(this.getLoadCommand());
		stateSpace.execute(new StartAnimationCommand());
	}

	/**
	 * @deprecated Use {@link #loadIntoStateSpace(StateSpace)} instead.
	 *     To control which component to load from a Rodin project,
	 *     pass the correct file name when loading the model,
	 *     or use {@link EventBModel#withMainComponent(AbstractElement)} to change the main component after loading.
	 * @param stateSpace the {@link StateSpace} into which to load the model
	 * @param mainComponent this model's main component, as returned by {@link ExtractedModel#getMainComponent()}
	 */
	@Deprecated
	public void loadIntoStateSpace(final StateSpace stateSpace, final AbstractElement mainComponent) {
		this.loadIntoStateSpace(stateSpace);
	}

	/**
	 * @deprecated Use {@link ExtractedModel#load(Map)} instead.
	 *     In the future, {@link AbstractModel}s will not include a {@link StateSpaceProvider}.
	 * @param mainComponent this model's main component, as returned by {@link ExtractedModel#getMainComponent()}
	 * @return the {@link StateSpace} for the loaded model
	 */
	@Deprecated
	public StateSpace load(AbstractElement mainComponent) {
		return load(mainComponent, new HashMap<>());
	}

	/**
	 * @deprecated Use {@link ExtractedModel#load(Map)} instead.
	 *     In the future, {@link AbstractModel}s will not include a {@link StateSpaceProvider}.
	 * @param mainComponent this model's main component, as returned by {@link ExtractedModel#getMainComponent()}
	 * @param preferences custom ProB preferences to use
	 * @return the {@link StateSpace} for the loaded model
	 */
	@Deprecated
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
