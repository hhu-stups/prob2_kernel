package de.prob.statespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.animator.IAnimator;
import de.prob.animator.IConsoleOutputListener;
import de.prob.animator.IWarningListener;
import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.CheckIfStateIdValidCommand;
import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.ExecuteOperationException;
import de.prob.animator.command.ExtendedStaticCheckCommand;
import de.prob.animator.command.FindStateCommand;
import de.prob.animator.command.FindTraceBetweenNodesCommand;
import de.prob.animator.command.FormulaTypecheckCommand;
import de.prob.animator.command.GetCurrentPreferencesCommand;
import de.prob.animator.command.GetDefaultPreferencesCommand;
import de.prob.animator.command.GetOperationByPredicateCommand;
import de.prob.animator.command.GetPreferenceCommand;
import de.prob.animator.command.GetShortestTraceCommand;
import de.prob.animator.command.GetStatesFromPredicate;
import de.prob.animator.command.IStateSpaceModifier;
import de.prob.animator.command.RegisterFormulasCommand;
import de.prob.animator.command.SetPreferenceCommand;
import de.prob.animator.command.UnregisterFormulasCommand;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.CSP;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.ProBPreference;
import de.prob.animator.domainobjects.TypeCheckResult;
import de.prob.annotations.MaxCacheSize;
import de.prob.formula.PredicateBuilder;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.eventb.EventBModel;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.CSPModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The StateSpace is where the animation of a given model is carried out. The
 * methods in the StateSpace allow the user to:
 *
 * 1) Find new states and operations
 *
 * 2) Inspect different states within the StateSpace
 *
 * 3) Evaluate custom predicates and expressions
 *
 * 4) Register listeners that are notified of new states and operations
 *
 * The implementation of the StateSpace is as a {@link StateSpace} with
 * {@link State}s as vertices and {@link Transition}s as edges. Therefore, some
 * basic graph functionalities are provided.
 *
 * @author joy
 *
 */
public class StateSpace implements IAnimator {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateSpace.class);

	private final IAnimator animator;

	private final Set<IEvalElement> registeredFormulas = new HashSet<>();
	private final Map<IEvalElement, Set<Object>> formulaSubscribers = new HashMap<>();

	/**
	 * The options to use for evaluating each subscribed formula.
	 * For now, it's not possible to subscribe the same formula multiple times with different options
	 * (but this probably isn't an important feature).
	 */
	private final Map<IEvalElement, EvalOptions> subscribedFormulaOptions = new HashMap<>();

	private LoadedMachine loadedMachine;

	private final LoadingCache<String, State> states;

	private AbstractModel model;
	private AbstractElement mainComponent;
	private volatile boolean killed;
	private final Collection<IStatesCalculatedListener> statesCalculatedListeners = new CopyOnWriteArrayList<>();

	@Inject
	public StateSpace(final Provider<IAnimator> panimator, @MaxCacheSize final int maxSize) {
		animator = panimator.get();
		states = CacheBuilder.newBuilder().maximumSize(maxSize).build(new CacheLoader<String, State>() {
			@Override
			public State load(final String key) {
				CheckIfStateIdValidCommand cmd = new CheckIfStateIdValidCommand(key);
				execute(cmd);
				if (cmd.isValidState()) {
					return new State(key, StateSpace.this);
				}
				throw new IllegalArgumentException(key + " does not represent a valid state in the StateSpace");
			}
		});
	}

	/**
	 * Retrieve the root state from the state space.
	 *
	 * @return the root state from the state space (it will be added to the
	 *         states cache if it doesn't yet exist)
	 */
	public State getRoot() {
		return addState("root");
	}

	/**
	 * Retrieve a state from the state space that has the specified state id.
	 *
	 * @param id
	 *            of the state to be retrieved
	 * @return the state object associated with the given id. This is added to
	 *         an internal cache.
	 * @throws IllegalArgumentException
	 *             if a state with the specified id doesn't exist
	 */
	public State getState(final String id) {
		try {
			return states.get(id);
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds a state with the specified id to the StateSpace (if it isn't already
	 * in the state space), and returns the state to the user.
	 *
	 * @param id
	 *            of the state to be retrieved
	 * @return a state object associated with the given id.
	 */
	State addState(final String id) {
		State sId = states.getIfPresent(id);
		if (sId != null) {
			return sId;
		}
		// This avoids the prolog query because this can only be called by
		// objects that know that this state id actually works.
		sId = new State(id, this);
		states.put(id, sId);
		return sId;
	}

	/**
	 * Most states in the state space use numeric ids. This method exists to
	 * allow the user to access a given state via integer id instead of string
	 * id. The integer value -1 maps to the root state (the only state id that
	 * is not a number)
	 *
	 * @param id
	 *            integer value of the state id to be retrieved
	 * @return a state associated with the id if one exists.
	 */
	public State getState(final int id) {
		if (id == -1) {
			return getRoot();
		}
		return getState(String.valueOf(id));
	}

	/**
	 * Whenever a {@link StateSpace} instance is created, it is assigned a
	 * unique identifier to help external parties differentiate between two
	 * instances. This getter method returns this id.
	 *
	 * @return the unique {@link String} id associated with this
	 *         {@link StateSpace} instance
	 */
	@Override
	public String getId() {
		return animator.getId();
	}

	/**
	 * <p>
	 * Find states for which a given predicate is true.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b> The returned list of states will also include states which
	 * are not initialised. The semantics of the method could therefore be
	 * better described as finding:
	 * </p>
	 * <p>
	 * <code>{states matching predicate} &cup; {noninitialised states}</code>
	 * </p>
	 *
	 *
	 * @param predicate
	 *            for which states will be found
	 * @return a {@link List} of any states found
	 */
	public List<State> getStatesFromPredicate(final IEvalElement predicate) {
		GetStatesFromPredicate cmd = new GetStatesFromPredicate(predicate);
		execute(cmd);
		List<String> ids = cmd.getIds();
		List<State> sIds = new ArrayList<>();
		for (String s : ids) {
			sIds.add(addState(s));
		}
		return sIds;
	}

	/**
	 * Takes the name of an operation and a predicate and finds Operations that
	 * satisfy the name and predicate at the given state. New Operations are
	 * added to the graph.
	 *
	 * @param state
	 *            {@link State} from which the operation should be found
	 * @param opName
	 *            name of the operation that should be executed
	 * @param predicate
	 *            an additional guard for the operation. This usually describes
	 *            the parameters
	 * @param nrOfSolutions
	 *            int number of solutions that should be found for the given
	 *            predicate
	 * @return list of operations calculated by ProB
	 */
	public List<Transition> transitionFromPredicate(final State state, final String opName, final IEvalElement predicate,
			final int nrOfSolutions) {
		final GetOperationByPredicateCommand command = new GetOperationByPredicateCommand(this, state.getId(), opName,
				predicate, nrOfSolutions);
		execute(command);
		if (command.hasErrors()) {
			if(command.getErrors().stream().allMatch(err -> err.getType() == GetOperationByPredicateCommand.GetOperationErrorType.CANNOT_EXECUTE)) {
				throw new ExecuteOperationException("Executing operation " + opName + " with additional predicate produced errors: " + String.join(", ", command.getErrorMessages()), command.getErrors());
			} else {
				throw new IllegalArgumentException("Executing operation " + opName + " with additional predicate produced parse errors: " + String.join(", ", command.getErrorMessages()));
			}
		}
		return command.getNewTransitions();
	}
	
	/**
	 * Same as {@link #transitionFromPredicate(State, String, IEvalElement, int)}.
	 * 
	 * @param state {@link State} from which the operation should be found
	 * @param opName name of the operation that should be executed
	 * @param predicate an additional guard for the operation. This usually describes the parameters
	 * @param nrOfSolutions int number of solutions that should be found for the given predicate
	 * @return list of operations calculated by ProB
	 */
	public List<Transition> transitionFromPredicate(final State state, final String opName, final String predicate,
		final int nrOfSolutions) {
		final IEvalElement pred = model.parseFormula(predicate);
		return this.transitionFromPredicate(state, opName, pred, nrOfSolutions);
	}

	public List<Transition> getTransitionsBasedOnParameterValues(final State state, final String opName,
			final List<String> parameterValues, final int nrOfSolutions) {
		if (Transition.isArtificialTransitionName(opName)) {
			throw new IllegalArgumentException(opName + " is a special operation and does not take positional arguments. Use transitionFromPredicate instead and specify the argument/variable/constant values as a predicate.");
		}

		if (!getLoadedMachine().containsOperations(opName)) {
			throw new IllegalArgumentException("Unknown operation '" + opName + "'");
		}
		OperationInfo machineOperationInfo = getLoadedMachine().getMachineOperationInfo(opName);
		List<String> parameterNames = machineOperationInfo.getParameterNames();
		final PredicateBuilder pb = new PredicateBuilder();
		if (!parameterNames.isEmpty()) {
			if (parameterNames.size() != parameterValues.size()) {
				throw new IllegalArgumentException("Cannot execute operation " + opName
						+ " because the number of parameters does not match the number of provied values: "
						+ parameterNames.size() + " vs " + parameterValues.size());
			}
			for (int i = 0; i < parameterNames.size(); i++) {
				pb.add(parameterNames.get(i), parameterValues.get(i));
			}
		}

		return this.transitionFromPredicate(state, opName, pb.toString(), nrOfSolutions);
	}

	/**
	 * Tests to see if a combination of an operation name and a predicate is
	 * valid from a given state.
	 *
	 * @param state
	 *            {@link State} id for state to test
	 * @param name
	 *            {@link String} name of operation
	 * @param predicate
	 *            {@link String} predicate to test
	 * @return true, if the operation is valid from the given state. False
	 *         otherwise.
	 */
	public boolean isValidOperation(final State state, final String name, final String predicate) {
		final ClassicalB pred = new ClassicalB(predicate);
		GetOperationByPredicateCommand command = new GetOperationByPredicateCommand(this, state.getId(), name, pred,
				1);
		execute(command);
		return !command.hasErrors();
	}

	/**
	 * Attempts to type check a specified formula in the scope of the model
	 * loaded in this state space.
	 *
	 * @param formula
	 *            to be type checked
	 * @return a {@link TypeCheckResult} representing the result of the parser
	 *         (containing the type of the formula and any errors that occurred)
	 */
	public TypeCheckResult typeCheck(final IEvalElement formula) {
		FormulaTypecheckCommand cmd = new FormulaTypecheckCommand(formula);
		execute(cmd);
		return cmd.getResult();
	}

	/**
	 * Evaluates a list of formulas in a given state. Uses the implementation in
	 * {@link State#eval(List)}
	 *
	 * @param state
	 *            for which the list of formulas should be evaluated
	 * @param formulas
	 *            to be evaluated
	 * @return a list of {@link AbstractEvalResult}s
	 * @deprecated Use {@link State#eval(List)} directly instead.
	 */
	@Deprecated
	public List<AbstractEvalResult> eval(final State state, final List<? extends IEvalElement> formulas) {
		return state.eval(formulas);
	}

	/**
	 * Calculates the registered formulas at the given state and returns the
	 * cached values. Calls the {@link State#explore()} method, and uses the
	 * {@link State#getValues()} method.
	 *
	 * @param state
	 *            for which the values are to be retrieved
	 * @return map from {@link IEvalElement} object to
	 *         {@link AbstractEvalResult} objects
	 * @deprecated Use {@link State#getValues()} directly instead,
	 *     possibly together with {@link State#explore()} or {@link State#exploreIfNeeded()}.
	 */
	@Deprecated
	public Map<IEvalElement, AbstractEvalResult> valuesAt(final State state) {
		state.explore();
		return state.getValues();
	}

	/**
	 * This checks if the {@link State#isInitialised()} property is set. If so,
	 * it is safe to evaluate formulas for the given state.
	 *
	 * @param state
	 *            which is to be tested
	 * @return whether or not formulas should be evaluated in this state
	 * @deprecated Use {@link State#isInitialised()} directly instead.
	 */
	@Deprecated
	public boolean canBeEvaluated(final State state) {
		return state.isInitialised();
	}

	/**
	 * Get all formulas currently registered for efficient evaluation.
	 * All {@linkplain #getSubscribedFormulas() subscribed formulas} are also automatically registered,
	 * but not vice versa.
	 * 
	 * @return set of all formulas currently registered for efficient evaluation
	 */
	public Set<IEvalElement> getRegisteredFormulas() {
		return Collections.unmodifiableSet(this.registeredFormulas);
	}

	/**
	 * <p>
	 * Register one or multiple formulas for efficient evaluation.
	 * Any formulas that were already registered previously will not be registered again.
	 * </p>
	 * <p>
	 * Registered formulas can be evaluated more efficiently,
	 * because the formula term is sent to Prolog and type-checked only once at registration time.
	 * However, registered formulas still need to be evaluated manually as needed.
	 * To evaluate formulas automatically in every new state, use {@link #subscribe(Object, Collection)}.
	 * </p>
	 * 
	 * @param formulas formulas to register for evaluation
	 * @return all formulas that were newly registered
	 */
	public Collection<IEvalElement> registerFormulas(final Collection<? extends IEvalElement> formulas) {
		final List<IEvalElement> toRegister = new ArrayList<>();
		for (final IEvalElement formula : formulas) {
			if (!this.registeredFormulas.contains(formula)) {
				toRegister.add(formula);
			}
		}
		
		if (!toRegister.isEmpty()) {
			final RegisterFormulasCommand cmd = new RegisterFormulasCommand(toRegister);
			this.execute(cmd);
			this.registeredFormulas.addAll(toRegister);
		}
		
		return toRegister;
	}

	/**
	 * Unregister one or multiple formulas for efficient evaluation.
	 * Any non-registered formulas in the list are silently ignored.
	 * 
	 * @param formulas formulas to unregister
	 */
	public void unregisterFormulas(final Collection<? extends IEvalElement> formulas) {
		final List<IEvalElement> toUnregister = new ArrayList<>();
		for (final IEvalElement formula : formulas) {
			if (this.registeredFormulas.contains(formula)) {
				toUnregister.add(formula);
			}
		}
		
		if (!toUnregister.isEmpty()) {
			this.execute(new UnregisterFormulasCommand(toUnregister));
			this.registeredFormulas.removeAll(toUnregister);
		}
	}

	/**
	 * This method lets ProB know that the subscriber is interested in the
	 * specified formulas. ProB will then evaluate the formulas for every state
	 * (after which the values can be retrieved from the
	 * {@link State#getValues()} method).
	 *
	 * @param subscriber
	 *            who is interested in the given formulas
	 * @param formulas
	 *            that are of interest
	 * @param options options to use when automatically evaluating the formulas - must be the same for all subscriptions of the same formula
	 * @return whether or not the subscription was successful (will return true
	 *         if at least one of the formulas was successfully subscribed)
	 */
	public boolean subscribe(final Object subscriber, final Collection<? extends IEvalElement> formulas, final EvalOptions options) {
		boolean success = false;
		List<IEvalElement> toSubscribe = new ArrayList<>();
		for (IEvalElement formulaOfInterest : formulas) {
			if (formulaOfInterest instanceof CSP) {
				LOGGER.info(
						"CSP formula {} not subscribed because CSP evaluation is not state based. Use eval method instead",
						formulaOfInterest.getCode());
			} else {
				if (formulaSubscribers.containsKey(formulaOfInterest)) {
					// Formula already exists in the map of subscribers - check that the options are compatible.
					assert subscribedFormulaOptions.containsKey(formulaOfInterest);
					final EvalOptions existingOptions = subscribedFormulaOptions.get(formulaOfInterest);

					if (!existingOptions.equals(options)) {
						// Formula already exists in the map and has incompatible options...
						if (formulaSubscribers.get(formulaOfInterest).isEmpty()) {
							// ...but has no subscribers anymore,
							// so it's safe to remove and replace with the new options.
							subscribedFormulaOptions.put(formulaOfInterest, options);
						} else {
							throw new IllegalArgumentException("Formula " + formulaOfInterest + " has already been subscribed in this state space with different evaluation options. Use the same evaluation options for all subscribers or remove the existing subscriber(s) first.");
						}
					}

					formulaSubscribers.get(formulaOfInterest).add(subscriber);
				} else {
					Set<Object> subscribers = Collections.newSetFromMap(new WeakHashMap<>());
					subscribers.add(subscriber);
					formulaSubscribers.put(formulaOfInterest, subscribers);
					subscribedFormulaOptions.put(formulaOfInterest, options);
					toSubscribe.add(formulaOfInterest);
				}
				success = true;
			}
		}
		this.registerFormulas(toSubscribe);
		return success;
	}

	/**
	 * This method lets ProB know that the subscriber is interested in the
	 * specified formulas. ProB will then evaluate the formulas for every state
	 * (after which the values can be retrieved from the
	 * {@link State#getValues()} method).
	 *
	 * @param subscriber
	 *            who is interested in the given formulas
	 * @param formulas
	 *            that are of interest
	 * @return whether or not the subscription was successful (will return true
	 *         if at least one of the formulas was successfully subscribed)
	 */
	public boolean subscribe(final Object subscriber, final Collection<? extends IEvalElement> formulas) {
		return this.subscribe(subscriber, formulas, EvalOptions.DEFAULT.withExpandFromFormulas(formulas));
	}

	/**
	 * If a class is interested in having a particular formula calculated and
	 * cached whenever a new state is explored, then they "subscribe" to that
	 * formula with a reference to themselves. This should only be used for
	 * B-Type formulas ({@code EventB} or {@code ClassicalB}). {@code CSP}
	 * formulas will not be subscribed, because CSP evaluation is not state
	 * based.
	 *
	 * @param subscriber
	 *            who is interested in the formula
	 * @param formulaOfInterest
	 *            that is to be subscribed
	 * @return will return true if the subscription was not successful, false
	 *         otherwise
	 */
	public boolean subscribe(final Object subscriber, final IEvalElement formulaOfInterest) {
		return this.subscribe(subscriber, Collections.singletonList(formulaOfInterest));
	}

	/**
	 * @param formula
	 *            to be checked
	 * @return whether or not a subscriber is interested in this formula
	 */
	public boolean isSubscribed(final IEvalElement formula) {
		return formulaSubscribers.containsKey(formula) && !formulaSubscribers.get(formula).isEmpty();
	}

	/**
	 * If a subscribed class is no longer interested in the value of a
	 * particular formula, then they can unsubscribe to that formula
	 *
	 * @param subscriber
	 *            who is no longer interested in the formulas
	 * @param formulas
	 *            that are to be unsubscribed
	 * @return whether or not the unsubscription was successful (will return
	 *         false if none of the formulas were subscribed to begin with)
	 */

	public boolean unsubscribe(final Object subscriber, final Collection<? extends IEvalElement> formulas) {
		return this.unsubscribe(subscriber, formulas, false);
	}

	public boolean unsubscribe(final Object subscriber, final Collection<? extends IEvalElement> formulas, boolean unregister) {
		boolean success = false;
		final List<IEvalElement> unsubscribeFormulas = new ArrayList<>();
		for (IEvalElement formula : formulas) {
			if (formulaSubscribers.containsKey(formula)) {
				final Set<Object> subscribers = formulaSubscribers.get(formula);
				subscribers.remove(subscriber);
				if (subscribers.isEmpty() && unregister) {
					unsubscribeFormulas.add(formula);
				}
				success = true;
			}
		}
		this.unregisterFormulas(unsubscribeFormulas);
		return success;
	}

	/**
	 * If a subscribed class is no longer interested in the value of a
	 * particular formula, then they can unsubscribe to that formula
	 *
	 * @param subscriber
	 *            who is no longer interested in the formula
	 * @param formula
	 *            which is to be unsubscribed
	 * @return whether or not the unsubscription was successful (will return
	 *         false if the formula was never subscribed to begin with)
	 */
	public boolean unsubscribe(final Object subscriber, final IEvalElement formula) {
		return this.unsubscribe(subscriber, Collections.singletonList(formula));
	}

	/**
	 * @return a {@link Set} containing the formulas for which there are
	 *         currently interested subscribers.
	 */
	public Set<IEvalElement> getSubscribedFormulas() {
		Set<IEvalElement> result = new HashSet<>();
		for (Map.Entry<IEvalElement, Set<Object>> entry : formulaSubscribers.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Get all formulas that currently have subscribers,
	 * grouped by the {@link EvalOptions} that were used when subscribing.
	 * 
	 * @return map of {@link EvalOptions} to all formulas that were subscribed with those options
	 */
	Map<EvalOptions, Set<IEvalElement>> getSubscribedFormulasByOptions() {
		final Map<EvalOptions, Set<IEvalElement>> result = new HashMap<>();
		for (final IEvalElement formula : this.getSubscribedFormulas()) {
			final EvalOptions options = subscribedFormulaOptions.get(formula);
			result.computeIfAbsent(options, k -> new HashSet<>()).add(formula);
		}
		return result;
	}

	/**
	 * Get information about the available ProB preferences in the animator.
	 * This only includes information that does not change during the execution of the animator,
	 * such as the preferences' type, default value, and description.
	 * In particular,
	 * the preferences' actual values are not included here and must be queried separately using {@link #getCurrentPreferences()}.
	 * 
	 * @return information about the available preferences in the animator
	 */
	public List<ProBPreference> getPreferenceInformation() {
		final GetDefaultPreferencesCommand cmd = new GetDefaultPreferencesCommand();
		this.execute(cmd);
		return cmd.getPreferences();
	}
	
	/**
	 * Get the current value of a single ProB preference.
	 * To get the current values of all preferences at once,
	 * use {@link #getCurrentPreferences()}
	 * 
	 * @param name the name of the preference whose value to get
	 * @return the current value of the preference
	 */
	public String getCurrentPreference(final String name) {
		final GetPreferenceCommand cmd = new GetPreferenceCommand(name);
		this.execute(cmd);
		return cmd.getValue();
	}

	/**
	 * Get the current values of all ProB preferences in the animator.
	 * 
	 * @return the current values of all ProB preferences in the animator
	 */
	public Map<String, String> getCurrentPreferences() {
		GetCurrentPreferencesCommand cmd = new GetCurrentPreferencesCommand();
		execute(cmd);
		return cmd.getPreferences();
	}

	/**
	 * Change the values of the given ProB preferences in the animator.
	 * Any preferences not listed in the map remain unchanged.
	 * 
	 * @param preferences the preference name/value pairs to change
	 */
	public void changePreferences(final Map<String, String> preferences) {
		final List<AbstractCommand> commands = new ArrayList<>();
		preferences.forEach((key, value) ->
			commands.add(new SetPreferenceCommand(key, value))
		);
		this.execute(new ComposedCommand(commands));
	}

	// ANIMATOR
	@Override
	public void sendInterrupt() {
		animator.sendInterrupt();
	}

	public void addStatesCalculatedListener(final IStatesCalculatedListener listener) {
		this.statesCalculatedListeners.add(listener);
	}

	public void removeStatesCalculatedListener(final IStatesCalculatedListener listener) {
		this.statesCalculatedListeners.remove(listener);
	}

	private void statesCalculated(final List<Transition> newTransitions) {
		this.statesCalculatedListeners.forEach(listener -> listener.newTransitions(newTransitions));
	}

	@Override
	public void execute(final AbstractCommand command) {
		animator.execute(command);
		if (command instanceof IStateSpaceModifier) {
			this.statesCalculated(((IStateSpaceModifier)command).getNewTransitions());
		}
	}

	@Override
	public void startTransaction() {
		animator.startTransaction();
	}

	@Override
	public void endTransaction() {
		animator.endTransaction();
	}

	@Override
	public boolean isBusy() {
		return animator.isBusy();
	}

	@Override
	public String toString() {
		return animator.getId();
	}

	public LoadedMachine getLoadedMachine() {
		if (this.loadedMachine == null) {
			loadedMachine = new LoadedMachine(this);
		}
		return this.loadedMachine;
	}

	/**
	 * @param state
	 *            whose operations are to be printed
	 * @return Returns a String representation of the operations available from
	 *         the specified {@link State}. This is mainly useful for console
	 *         output.
	 */
	public String printOps(final State state) {
		final StringBuilder sb = new StringBuilder();
		final Collection<Transition> opIds = state.getTransitions();

		sb.append("Operations: \n");
		for (final Transition opId : opIds) {
			sb.append("  ").append(opId.getId()).append(": ").append(opId.getRep());
			sb.append("\n");
		}

		if (!state.isExplored()) {
			sb.append("\n Possibly not all transitions shown. ProB does not explore states by default");
		}
		return sb.toString();
	}

	/**
	 * @param state
	 *            which is to be printed
	 * @return Returns a String representation of the information about the
	 *         state with the specified {@link State}. This includes the id for
	 *         the state, the cached calculated values, and if an invariant
	 *         violation or a timeout has occured for the given state. This is
	 *         mainly useful for console output.
	 */
	public String printState(final State state) {
		final StringBuilder sb = new StringBuilder();

		state.explore();

		sb.append("STATE: ").append(state).append("\n\n");
		sb.append("VALUES:\n");
		Map<IEvalElement, AbstractEvalResult> currentState = state.getValues();
		final Set<Map.Entry<IEvalElement, AbstractEvalResult>> entrySet = currentState.entrySet();
		for (final Map.Entry<IEvalElement, AbstractEvalResult> entry : entrySet) {
			sb.append("  ").append(entry.getKey().getCode()).append(" -> ").append(entry.getValue().toString()).append("\n");
		}

		return sb.toString();
	}

	/**
	 * This calculated the shortest path from root to the specified state.
	 *
	 * @param stateId
	 *            state id for which the trace through the state space should be
	 *            found.
	 * @return trace in the form of a {@link Trace} object
	 */
	public Trace getTrace(final String stateId) {
		GetShortestTraceCommand cmd = new GetShortestTraceCommand(this, stateId);
		execute(cmd);
		return cmd.getTrace(this);
	}

	/**
	 * Calculates a trace between the specified states.
	 *
	 * @param sourceId
	 *            of source node
	 * @param destId
	 *            of destination node
	 * @return shortest Trace between the two specified ids (in form of
	 *         {@link Trace} object)
	 */
	public Trace getTrace(final String sourceId, final String destId) {
		FindTraceBetweenNodesCommand cmd = new FindTraceBetweenNodesCommand(this, sourceId, destId);
		execute(cmd);
		return cmd.getTrace(this);
	}

	/**
	 * Takes a list of {@link String} operation id names and generates a
	 * {@link Trace} by executing each one in order. This calls the
	 * {@link Trace#add(String)} method which can throw an
	 * {@link IllegalArgumentException} if executing the operations in the
	 * specified order is not possible. It assumes that the Trace begins from
	 * the root state.
	 *
	 * @param transitionIds
	 *            List of transition ids in the order that they should be
	 *            executed.
	 * @return {@link Trace} generated by executing the ids.
	 */
	public Trace getTrace(final List<String> transitionIds) {
		Trace t = new Trace(this);
		for (String id : transitionIds) {
			t = t.add(id);
		}
		return t;
	}

	/**
	 * <p>
	 * This allows developers to programmatically describe a Trace that should
	 * be created. {@link ITraceDescription#getTrace(StateSpace)} will then be
	 * called in order to generate the correct Trace.
	 * </p>
	 *
	 * @param description
	 *            of the trace to be created
	 * @return Trace that is generated from the Trace Description
	 * @deprecated This overload is meant for use from Groovy.
	 *     Java code should call {@link ITraceDescription#getTrace(StateSpace)} directly instead.
	 */
	// TODO Move this to ProBGroovyMethods at some point (or just remove it entirely - I don't think anything really needs this overload...)
	@Deprecated
	public Trace getTrace(final ITraceDescription description) {
		return description.getTrace(this);
	}

	/**
	 * Takes an {@link IEvalElement} containing a predicate and returns a
	 * {@link Trace} containing only a magic operation that leads to valid state
	 * where the predicate holds.
	 *
	 * @param predicate
	 *            predicate that should hold in the valid state
	 * @return {@link Trace} containing a magic operation leading to the state.
	 */
	public Trace getTraceToState(final IEvalElement predicate) {
		FindStateCommand cmd = new FindStateCommand(this, predicate, true);
		execute(cmd);
		return cmd.getTrace(this);
	}

	/**
	 * Set the model that is being animated. This should only be set at the
	 * beginning of an animation. The currently supported model types are
	 * {@link ClassicalBModel}, {@link EventBModel}, or {@link CSPModel}. A
	 * StateSpace object always corresponds with exactly one model.
	 *
	 * @param model
	 *            the new model
	 * @param mainComponent
	 *            the new main component
	 * @deprecated Use {@link #initModel(AbstractModel, AbstractElement)} instead
	 */
	@Deprecated
	public void setModel(final AbstractModel model, final AbstractElement mainComponent) {
		this.model = model;
		this.mainComponent = mainComponent;
	}

	/**
	 * Set the model that is being animated. This should be set at the
	 * beginning of an animation and can only be set once per StateSpace.
	 * A StateSpace object always corresponds to exactly one model.
	 *
	 * @param model the new model
	 * @param mainComponent the new main component
	 * @throws IllegalStateException if the model or main component has already been set
	 */
	public void initModel(final AbstractModel model, final AbstractElement mainComponent) {
		if (this.getModel() != null) {
			throw new IllegalStateException("model has already been set");
		}
		if (this.getMainComponent() != null) {
			throw new IllegalStateException("mainComponent has already been set");
		}
		this.setModel(model, mainComponent);
	}

	/**
	 * Returns the specified model for the given StateSpace.
	 *
	 * @return the {@link AbstractModel} that represents the model for the given
	 *         StateSpace instance
	 */
	public AbstractModel getModel() {
		return model;
	}

	public AbstractElement getMainComponent() {
		return mainComponent;
	}

	/**
	 * Takes a collection of transitions and retrieves any information that
	 * needs to be retrieved (i.e. parameters, return values, etc.) if the
	 * transitions have not yet been evaluated
	 * ({@link Transition#isEvaluated(EvalOptions)}).
	 *
	 * @param transitions the transitions to be evaluated
	 * @param options options for evaluation
	 * @return map of all transitions and the corresponding evaluated infos
	 */
	public Map<Transition, EvaluatedTransitionInfo> evaluateTransitions(final Collection<Transition> transitions, final EvalOptions options) {
		List<GetOpFromId> opInfoCmds = new ArrayList<>();
		for (Transition transition : transitions) {
			if (!transition.isEvaluated(options)) {
				opInfoCmds.add(new GetOpFromId(transition, options));
			}
		}
		execute(opInfoCmds);
		final Map<Transition, EvaluatedTransitionInfo> result = new LinkedHashMap<>(transitions.size());
		for (final Transition transition : transitions) {
			result.put(transition, transition.evaluate(options));
		}
		return result;
	}

	/**
	 * Takes a collection of transitions and retrieves any information that
	 * needs to be retrieved (i.e. parameters, return values, etc.) if the
	 * transitions have not yet been evaluated
	 * ({@link Transition#isEvaluated()}).
	 *
	 * @param transitions
	 *            the transitions to be evaluated
	 * @param expansion
	 *            how formulas should be expanded
	 * @return a set containing all of the evaluated transitions
	 */
	public Set<Transition> evaluateTransitions(final Collection<Transition> transitions,
			final FormulaExpand expansion) {
		this.evaluateTransitions(transitions, Transition.OLD_DEFAULT_EVAL_OPTIONS.withExpand(expansion));
		return new LinkedHashSet<>(transitions);
	}

	/**
	 * Evaluates all of the formulas for every specified state.
	 * If a formula has already been evaluated in a state,
	 * e. g. because an object has subscribed to the formula,
	 * the cached value is reused and the formula is not re-evaluated.
	 *
	 * @param states
	 *            for which the formula is to be evaluated
	 * @param formulas
	 *            which are to be evaluated
	 * @param options options for evaluation
	 * @return a map of the formulas and their results for all of the specified
	 *         states
	 */
	public Map<State, Map<IEvalElement, AbstractEvalResult>> evaluateForGivenStates(final Collection<State> states,
			final List<IEvalElement> formulas, final EvalOptions options) {
		Map<State, Map<IEvalElement, AbstractEvalResult>> result = new HashMap<>();

		for (State state : states) {
			// TODO Optimize this again by sending all evaluation commands at once
			result.put(state, state.evalFormulas(formulas, options));
		}

		return result;
	}
	
	/**
	 * Evaluates all of the formulas for every specified state.
	 * If a formula has already been evaluated in a state,
	 * e. g. because an object has subscribed to the formula,
	 * the cached value is reused and the formula is not re-evaluated.
	 *
	 * @param states for which the formula is to be evaluated
	 * @param formulas which are to be evaluated
	 * @return a map of the formulas and their results for all of the specified states
	 */
	public Map<State, Map<IEvalElement, AbstractEvalResult>> evaluateForGivenStates(final Collection<State> states, final List<IEvalElement> formulas) {
		return this.evaluateForGivenStates(states, formulas, EvalOptions.DEFAULT.withExpandFromFormulas(formulas));
	}

	/**
	 * Run extended static checks on the loaded machine.
	 * 
	 * @return a list of all problems found by the extended static check
	 */
	public List<ErrorItem> performExtendedStaticChecks() {
		final ExtendedStaticCheckCommand cmd = new ExtendedStaticCheckCommand();
		this.execute(cmd);
		return cmd.getProblems();
	}

	@Override
	public void kill() {
		killed = true;
		animator.kill();
	}

	public boolean isKilled() {
		return killed;
	}

	/**
	 * This method cannot be used on a {@link StateSpace} - you probably want {@link #kill()}.
	 * Resetting ProB clears the currently loaded model (among other things),
	 * which would break the {@link StateSpace} instance as it expects a specific model.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	@Override
	public void resetProB() {
		throw new UnsupportedOperationException("Cannot reset the ProB instance belonging to an active StateSpace");
	}

	@Override
	public long getTotalNumberOfErrors() {
		return animator.getTotalNumberOfErrors();
	}

	@Override
	public void addWarningListener(final IWarningListener listener) {
		animator.addWarningListener(listener);
	}

	@Override
	public void removeWarningListener(final IWarningListener listener) {
		animator.removeWarningListener(listener);
	}

	@Override
	public void addConsoleOutputListener(final IConsoleOutputListener listener) {
		animator.addConsoleOutputListener(listener);
	}

	@Override
	public void removeConsoleOutputListener(final IConsoleOutputListener listener) {
		animator.removeConsoleOutputListener(listener);
	}
}
