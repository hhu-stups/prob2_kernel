package de.prob.statespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.prob.animator.command.EvaluateFormulasCommand;
import de.prob.animator.command.ExecuteOperationException;
import de.prob.animator.command.ExploreStateCommand;
import de.prob.animator.command.GetBStateCommand;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.StateError;
import de.prob.model.representation.AbstractModel;

/**
 * A reference to the state object in the ProB core.
 *
 * Note: This class contains a reference to the StateSpace object to which this state
 * reference belongs. In order for the garbage collector to work correctly, dereference
 * any State objects after they are no longer needed.
 *
 * @author joy
 */
public class State {
	private final String id;
	private final StateSpace stateSpace;
	private volatile boolean explored;
	private List<Transition> transitions;
	private boolean constantsSetUp;
	private boolean initialised;
	private boolean invariantOk;
	private boolean timeoutOccurred;
	private Set<String> transitionsWithTimeout;
	private boolean maxTransitionsCalculated;
	private Collection<StateError> stateErrors;

	/**
	 * Internal cache of evaluated formula values in this state,
	 * grouped by the {@link EvalOptions} that were used for evaluation.
	 * The {@link IEvalElement#expansion()} is <i>not</i> directly used here.
	 * Code that uses this cache is expected to integrate that setting into the {@link EvalOptions} as appropriate.
	 */
	private final Map<EvalOptions, Map<IEvalElement, AbstractEvalResult>> evalCache;

	public State(String id, StateSpace space) {
		this.id = id;
		this.stateSpace = space;
		this.explored = false;
		this.transitions = new ArrayList<>();
		this.evalCache = new HashMap<>();
	}

	/**
	 * Uses {@link State#perform(String, List)} to calculate the destination state of the event
	 * with the specified event name and the conjunction of the parameters.
	 * An exception will be thrown if the specified event and params are invalid for this State.
	 * @param event String event name to execute
	 * @param predicates List of String predicates
	 * @return {@link State} that results from executing the specified event
	 */
	public State perform(String event, String... predicates) {
		return perform(event, Arrays.asList(predicates));
	}

	/**
	 * Uses {@link State#findTransition(String, List)} to calculate the destination state of the event
	 * with the specified event name and the conjunction of the parameters.
	 * An exception will be thrown if the specified event and predicates are invalid for this State.
	 * @param event String event name to execute
	 * @param predicates List of String predicates
	 * @return {@link State} that results from executing the specified event
	 */
	public State perform(String event, List<String> predicates) {
		final Transition op = findTransition(event, predicates);
		if (op == null) {
			throw new IllegalArgumentException("Could not execute " + event + " with predicates " + predicates + " on state " + this.getId());
		}
		return op.getDestination();
	}

	/**
	 * Calls {@link State#findTransition(String, List)}
	 * @param name of the operation
	 * @param predicates list of predicates specifying the parameters
	 * @return the calculated transition, or null if no transition was found.
	 */
	public Transition findTransition(String name, String... predicates) {
		return findTransition(name, Arrays.asList(predicates));
	}

	/**
	 * Calls {@link State#findTransitions(String, List, int)}
	 * @param name of the operation
	 * @param predicates list of predicates specifying the parameters
	 * @return the calculated transition, or null if no transition was found.
	 */
	public Transition findTransition(final String name, List<String> predicates) {
		if (predicates.isEmpty() && !transitions.isEmpty()) {
			final Optional<Transition> op = transitions.stream().filter(t -> t.getName().equals(name)).findAny();
			if (op.isPresent()) {
				return op.get();
			}
		}
		final List<Transition> transitions = findTransitions(name, predicates, 1);
		if (!transitions.isEmpty()) {
			return transitions.get(0);
		}
		return null;
	}

	/**
	 * Calls {@link StateSpace#transitionFromPredicate(State, String, String, int)}
	 * @param name of the operation
	 * @param predicates list of predicates specifying the parameters
	 * @param nrOfSolutions to be found
	 * @return a list of solutions found, or an empty list if no solutions were found
	 */
	public List<Transition> findTransitions(String name, List<String> predicates, int nrOfSolutions) {
		final String predicate = predicates.isEmpty() ? "TRUE = TRUE" : '(' + String.join(") & (", predicates) + ')';
		List<Transition> newOps;
		try {
			newOps = stateSpace.transitionFromPredicate(this, name, predicate, nrOfSolutions);
			transitions.addAll(newOps);
		} catch (ExecuteOperationException e) {
			return new ArrayList<>();
		}
		return newOps;
	}

	// TODO This duplicates Trace.anyOperation (almost, but not exactly)
	public State anyOperation(final Object filter) {
		List<Transition> ops = getOutTransitions();
		if (filter instanceof String) {
			final Pattern filterPattern = Pattern.compile((String)filter);
			ops = ops.stream().filter(t -> filterPattern.matcher(t.getName()).matches()).collect(Collectors.toList());
		}
		if (filter instanceof ArrayList) {
			ops = ops.stream().filter(t -> ((List<?>)filter).contains(t.getName())).collect(Collectors.toList());
		}
		if (!ops.isEmpty()) {
			Collections.shuffle(ops);
			final Transition op = ops.get(0);
			final State newState = op.getDestination();
			newState.exploreIfNeeded();
			return newState;
		}
		return this;
	}

	public State anyEvent(Object filter) {
		return anyOperation(filter);
	}

	private Map<IEvalElement, AbstractEvalResult> getEvalCacheForOptions(final EvalOptions options) {
		return this.evalCache.computeIfAbsent(options, k -> new HashMap<>());
	}

	/**
	 * Takes a formula and evaluates it via the {@link State#eval(IEvalElement)}
	 * method. The formula is parsed via the {@link AbstractModel#parseFormula(String)} method.
	 * @param formula representation of a formula
	 * @return the {@link AbstractEvalResult} calculated from ProB
	 */
	public AbstractEvalResult eval(String formula, EvalOptions options) {
		return eval(stateSpace.getModel().parseFormula(formula, options.getExpand()), options);
	}

	/**
	 * Takes a formula and evaluates it via the {@link State#eval(IEvalElement)}
	 * method. The formula is parsed via the {@link AbstractModel#parseFormula(String)} method.
	 * @param formula representation of a formula
	 * @return the {@link AbstractEvalResult} calculated from ProB
	 */
	public AbstractEvalResult eval(String formula, FormulaExpand expand) {
		return eval(formula, EvalOptions.DEFAULT.withExpand(expand));
	}
	
	/**
	 * Takes a formula and evaluates it via the {@link State#eval(IEvalElement)}
	 * method. The formula is parsed via the {@link AbstractModel#parseFormula(String)} method.
	 * @param formula representation of a formula
	 * @return the {@link AbstractEvalResult} calculated from ProB
	 */
	public AbstractEvalResult eval(String formula) {
		return this.eval(formula, FormulaExpand.TRUNCATE);
	}

	/**
	 * Takes a formula and evaluates it via the {@link State#eval(List)} method.
	 * @param formula as IEvalElement
	 * @param options options for evaluation
	 * @return the {@link AbstractEvalResult} calculated by ProB
	 */
	public AbstractEvalResult eval(IEvalElement formula, EvalOptions options) {
		return eval(Collections.singletonList(formula), options).get(0);
	}

	/**
	 * Takes a formula and evaluates it via the {@link State#eval(List)} method.
	 * @param formula as IEvalElement
	 * @return the {@link AbstractEvalResult} calculated by ProB
	 */
	public AbstractEvalResult eval(IEvalElement formula) {
		return this.eval(formula, EvalOptions.DEFAULT.withExpand(formula.expansion()));
	}

	public List<AbstractEvalResult> eval(IEvalElement... formulas) {
		return eval(Arrays.asList(formulas));
	}

	public Map<IEvalElement, AbstractEvalResult> getVariableValues(final EvalOptions options) {
		return evalFormulas(stateSpace.getLoadedMachine().getVariableEvalElements(options.getExpand()), options);
	}

	public Map<IEvalElement, AbstractEvalResult> getVariableValues(final FormulaExpand expand) {
		return this.getVariableValues(EvalOptions.DEFAULT.withExpand(expand));
	}

	public Map<IEvalElement, AbstractEvalResult> getConstantValues(final EvalOptions options) {
		return evalFormulas(stateSpace.getLoadedMachine().getConstantEvalElements(options.getExpand()), options);
	}

	public Map<IEvalElement, AbstractEvalResult> getConstantValues(final FormulaExpand expand) {
		return this.getConstantValues(EvalOptions.DEFAULT.withExpand(expand));
	}

	/**
	 * Evaluate multiple formulas in this state.
	 * 
	 * @param formulas the formulas to evaluate
	 * @param options options for evaluation
	 * @return map of formulas to their values in this state
	 */
	public Map<IEvalElement, AbstractEvalResult> evalFormulas(List<? extends IEvalElement> formulas, EvalOptions options) {
		final Map<IEvalElement, AbstractEvalResult> cache = this.getEvalCacheForOptions(options);
		final List<IEvalElement> notEvaluatedElements = new ArrayList<>();
		for (IEvalElement element : formulas) {
			if (!cache.containsKey(element)) {
				notEvaluatedElements.add(element);
			}
		}
		if (!notEvaluatedElements.isEmpty()) {
			final EvaluateFormulasCommand cmd = new EvaluateFormulasCommand(notEvaluatedElements, this, options);
			stateSpace.execute(cmd);
			cache.putAll(cmd.getResultMap());
		}

		Map<IEvalElement, AbstractEvalResult> result = new LinkedHashMap<>();
		for (IEvalElement element : formulas) {
			result.put(element, cache.get(element));
		}
		return result;
	}

	/**
	 * Evaluate multiple formulas in this state.
	 *
	 * @param formulas the formulas to evaluate
	 * @return map of formulas to their values in this state
	 */
	public Map<IEvalElement, AbstractEvalResult> evalFormulas(List<? extends IEvalElement> formulas) {
		return this.evalFormulas(formulas, EvalOptions.DEFAULT.withExpandFromFormulas(formulas));
	}

	/**
	 * @param formulas to be evaluated
	 * @param options options for evaluation
	 * @return list of results calculated by ProB for a given formula
	 */
	public List<AbstractEvalResult> eval(List<? extends IEvalElement> formulas, EvalOptions options) {
		return new ArrayList<>(evalFormulas(formulas, options).values());
	}

	/**
	 * @param formulas to be evaluated
	 * @return list of results calculated by ProB for a given formula
	 */
	public List<AbstractEvalResult> eval(List<? extends IEvalElement> formulas) {
		return new ArrayList<>(evalFormulas(formulas).values());
	}

	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return id;
	}

	public long numericalId() {
		return "root".equals(id) ? -1 : Long.parseLong(id);
	}

	public String getStateRep() {
		if (stateSpace.getModel().getFormalismType().equals(FormalismType.B)) {
			final GetBStateCommand cmd = new GetBStateCommand(this);
			stateSpace.execute(cmd);
			return cmd.getState();
		}
		return "unknown";
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final State other = (State)obj;
		return Objects.equals(this.getId(), other.getId()) && Objects.equals(this.getStateSpace(), other.getStateSpace());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getId(), this.getStateSpace());
	}

	public StateSpace getStateSpace() {
		return stateSpace;
	}

	public boolean isExplored() {
		return explored;
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public boolean isConstantsSetUp() {
		this.exploreIfNeeded();
		return constantsSetUp;
	}

	public boolean isInitialised() {
		this.exploreIfNeeded();
		return initialised;
	}

	public boolean isInvariantOk() {
		this.exploreIfNeeded();
		return invariantOk;
	}

	public boolean isMaxTransitionsCalculated() {
		this.exploreIfNeeded();
		return maxTransitionsCalculated;
	}

	public boolean isTimeoutOccurred() {
		this.exploreIfNeeded();
		return timeoutOccurred;
	}

	public Set<String> getTransitionsWithTimeout() {
		this.exploreIfNeeded();
		return transitionsWithTimeout;
	}

	public Collection<StateError> getStateErrors() {
		this.exploreIfNeeded();
		return stateErrors;
	}

	/**
	 * If the state has not yet been explored (i.e. the default number
	 * of outgoing transitions has not yet been calculated by ProB), this
	 * is done via the {@link State#explore()} method. The list of
	 * {@link Transition} objects created will not be evaluated, i.e. certain
	 * information about the transition will be lazily retrieved from ProB
	 * at a later time. To evaluate the {@link Transition} objects eagerly,
	 * pass the returned list to {@link StateSpace#evaluateTransitions(Collection, EvalOptions)}.
	 * 
	 * @return the outgoing transitions from this state
	 */
	public List<Transition> getOutTransitions() {
		this.exploreIfNeeded();
		return this.transitions;
	}

	/**
	 * @deprecated Use {@link #getOutTransitions()} instead.
	 *     If {@code evaluate} was set to {@code true},
	 *     also call {@link StateSpace#evaluateTransitions(Collection, EvalOptions)} on the returned list.
	 */
	@Deprecated
	public List<Transition> getOutTransitions(boolean evaluate) {
		return this.getOutTransitions(evaluate, FormulaExpand.TRUNCATE);
	}

	/**
	 * If the state has not yet been explored (i.e. the default number
	 * of outgoing transitions has not yet been calculated by ProB), this
	 * is done via the {@link State#explore()} method. By default, the list of
	 * {@link Transition} objects created will not be evaluated (i.e. certain
	 * information about the transition will be lazily retrieved from ProB
	 * at a later time). However, if an optional parameter is supplied and
	 * set to true, the evaluation of all of the {@link Transition} objects will
	 * occur before the list is returned via the {@link StateSpace#evaluateTransitions(Collection, FormulaExpand)}
	 * method.
	 * @param evaluate whether or not the list of transitions should be evaluated. By default this is set to false.
	 * @return the outgoing transitions from this state
	 * @deprecated Use {@link #getOutTransitions()} instead.
	 *     If {@code evaluate} was set to {@code true},
	 *     also call {@link StateSpace#evaluateTransitions(Collection, FormulaExpand)} on the returned list.
	 */
	@Deprecated
	public List<Transition> getOutTransitions(boolean evaluate, FormulaExpand expansion) {
		this.exploreIfNeeded();
		if (evaluate) {
			stateSpace.evaluateTransitions(transitions, expansion);
		}
		return transitions;
	}

	public synchronized State explore() {
		final ExploreStateCommand cmd = new ExploreStateCommand(stateSpace, id, Collections.emptyList());
		stateSpace.execute(cmd);
		transitions = cmd.getNewTransitions();
		constantsSetUp = cmd.isConstantsSetUp();
		initialised = cmd.isInitialised();
		invariantOk = cmd.isInvariantOk();
		timeoutOccurred = cmd.isTimeoutOccured();
		maxTransitionsCalculated = cmd.isMaxOperationsReached();
		stateErrors = cmd.getStateErrors();
		transitionsWithTimeout = cmd.getOperationsWithTimeout();
		// TODO Combine the subscribed formula evaluation commands into ExploreStateCommand again
		this.getValues();
		explored = true;
		return this;
	}

	/**
	 * Ensures that this state is explored.
	 * Calls {@link #explore()} if the state hasn't been explored yet,
	 * otherwise does nothing.
	 * 
	 * @return {@code this}
	 */
	public State exploreIfNeeded() {
		// Avoid locking if the state has already been explored.
		if (!explored) {
			synchronized (this) {
				// Check again in case another thread already explored the state while this thread waited on the lock.
				if (!explored) {
					this.explore();
				}
			}
		}
		return this;
	}

	/**
	 * Get the values of all formulas subscribed in the {@link StateSpace}.
	 * The values are cached if possible.
	 * Evaluation uses the {@link EvalOptions} that were specified when the formula was subscribed.
	 * 
	 * @return values of all subscribed formulas
	 */
	public Map<IEvalElement, AbstractEvalResult> getValues() {
		final Map<IEvalElement, AbstractEvalResult> flatValues = new HashMap<>();
		stateSpace.getSubscribedFormulasByOptions().forEach((options, formulas) ->
			flatValues.putAll(this.evalFormulas(new ArrayList<>(formulas), options))
		);
		return flatValues;
	}
}
