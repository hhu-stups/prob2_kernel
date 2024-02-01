package de.prob.statespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.krukow.clj_lang.PersistentVector;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractModel;

/**
 * @author joy
 */
public class Trace {
	private boolean exploreStateByDefault = true;
	private final TraceElement current;
	private final TraceElement head;
	private final StateSpace stateSpace;
	private final UUID uuid;
	private final PersistentVector<Transition> transitionList;

	public Trace(final StateSpace s) {
		this(s.getRoot());
	}

	public Trace(final State startState) {
		this(startState.getStateSpace(), new TraceElement(startState), PersistentVector.emptyVector(), UUID.randomUUID());
	}

	public Trace(final StateSpace s, final TraceElement head, List<Transition> transitionList, UUID uuid) {
		this(s, head, head, transitionList, uuid);
	}

	private Trace(final StateSpace s, final TraceElement head, final TraceElement current, List<Transition> transitionList, UUID uuid) {
		this.stateSpace = s;
		this.head = head;
		this.current = current;
		if (transitionList instanceof PersistentVector<?>) {
			this.transitionList = (PersistentVector<Transition>)transitionList;
		} else {
			this.transitionList = PersistentVector.create(transitionList);
		}
		this.uuid = uuid;
	}

	public boolean isExploreStateByDefault() {
		return exploreStateByDefault;
	}

	public void setExploreStateByDefault(boolean exploreStateByDefault) {
		this.exploreStateByDefault = exploreStateByDefault;
	}

	public final TraceElement getCurrent() {
		return current;
	}

	public final TraceElement getHead() {
		return head;
	}

	public final UUID getUUID() {
		return uuid;
	}

	/**
	 * Get a list of all {@link TraceElement}s that make up this trace.
	 * Currently, this list is newly constructed on every call to this method and is <i>not</i> cached.
	 * To iterate over the trace more efficiently,
	 * traverse the {@link TraceElement}s manually starting at {@link #getHead()} or {@link #getCurrent()}.
	 * 
	 * @return list of all elements of this trace
	 */
	public List<TraceElement> getElements() {
		final LinkedList<TraceElement> elements = new LinkedList<>();
		for (TraceElement element = this.getHead(); element != null; element = element.getPrevious()) {
			elements.addFirst(element);
		}
		return elements;
	}

	public AbstractEvalResult evalCurrent(String formula, EvalOptions options) {
		return getCurrentState().eval(formula, options);
	}

	public AbstractEvalResult evalCurrent(String formula, FormulaExpand expand) {
		return getCurrentState().eval(formula, expand);
	}
	
	public AbstractEvalResult evalCurrent(String formula) {
		return getCurrentState().eval(formula);
	}

	public AbstractEvalResult evalCurrent(IEvalElement formula, EvalOptions options) {
		return getCurrentState().eval(formula, options);
	}

	public AbstractEvalResult evalCurrent(IEvalElement formula) {
		return getCurrentState().eval(formula);
	}

	public int size() {
		return transitionList.size();
	}

	/**
	 * Evaluate a formula over all states in this trace
	 * (including uninitialized states like the root state).
	 * If multiple {@link TraceElement}s correspond to the same state,
	 * all of them are included in the result map,
	 * but internally the formula is only evaluated once per state.
	 * 
	 * @param formula the formula to evaluate
	 * @param options options for evaluation
	 * @return ordered map of all {@link TraceElement}s and the value of {@code formula} in the corresponding state
	 */
	public Map<TraceElement, AbstractEvalResult> evalAll(final IEvalElement formula, final EvalOptions options) {
		final List<TraceElement> elements = this.getElements();
		final Set<State> states = new HashSet<>();
		for (final TraceElement element : elements) {
			states.add(element.getCurrentState());
		}
		final Map<State, Map<IEvalElement, AbstractEvalResult>> resultsByState = stateSpace.evaluateForGivenStates(states, Collections.singletonList(formula), options);
		final Map<TraceElement, AbstractEvalResult> result = new LinkedHashMap<>();
		for (final TraceElement element : elements) {
			result.put(element, resultsByState.get(element.getCurrentState()).get(formula));
		}
		return result;
	}

	/**
	 * Evaluate a formula over all states in this trace
	 * (including uninitialized states like the root state).
	 * If multiple {@link TraceElement}s correspond to the same state,
	 * all of them are included in the result map,
	 * but internally the formula is only evaluated once per state.
	 * 
	 * @param formula the formula to evaluate
	 * @return ordered map of all {@link TraceElement}s and the value of {@code formula} in the corresponding state
	 */
	public Map<TraceElement, AbstractEvalResult> evalAll(final IEvalElement formula) {
		return this.evalAll(formula, EvalOptions.DEFAULT);
	}

	/**
	 * Evaluate a formula over all states in this trace
	 * (including uninitialized states like the root state).
	 * If multiple {@link TraceElement}s correspond to the same state,
	 * all of them are included in the result map,
	 * but internally the formula is only evaluated once per state.
	 * 
	 * @param formula the formula to evaluate
	 * @return ordered map of all {@link TraceElement}s and the value of {@code formula} in the corresponding state
	 */
	public Map<TraceElement, AbstractEvalResult> evalAll(final String formula) {
		return this.evalAll(stateSpace.getModel().parseFormula(formula), EvalOptions.DEFAULT);
	}

	public Trace add(final Transition op) {
		// TODO: Should we check to ensure that current.getCurrentState() == op.getSrcId()
		final TraceElement newHE = new TraceElement(op, current);
		final PersistentVector<Transition> transitionList = branchTransitionListIfNecessary(op);
		final Trace newTrace = new Trace(stateSpace, newHE, transitionList, this.uuid);
		newTrace.setExploreStateByDefault(this.exploreStateByDefault);
		if (exploreStateByDefault) {
			op.getDestination().exploreIfNeeded();
		}
		return newTrace;
	}

	public Trace add(final String transitionId) {
		final Transition op = getCurrentState().getOutTransitions().stream()
			.filter(t -> t.getId().equals(transitionId))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(transitionId + " is not a valid operation on this state"));
		return add(op);
	}

	public Trace add(final int i) {
		return add(String.valueOf(i));
	}

	/**
	 * Tries to find an operation with the specified name and parameters in the
	 * list of transitions calculated by ProB.
	 * @param name of the event to be executed
	 * @param parameters values of the parameters for the event
	 * @return a new trace with the operation added.
	 */
	public Trace addTransitionWith(final String name, final List<String> parameters) {
		final List<Transition> transitions = getCurrentState().getOutTransitions().stream()
			.filter(it -> it.getName().equals(name))
			.collect(Collectors.toList());
		stateSpace.evaluateTransitions(transitions, FormulaExpand.EXPAND);
		Transition op = transitions.stream()
			.filter(it -> it.getParameterValues().equals(parameters))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Could not find operation " + name + " with parameters " + parameters));
		/* TODO: call GetOperationByPredicateCommand when MAX_OPERATIONS reached */
		return add(op);
	}

	/**
	 * Moves one step back in the animation if this is possible.
	 */
	public Trace back() {
		if (canGoBack()) {
			return new Trace(stateSpace, head, current.getPrevious(), transitionList, this.uuid);
		}
		return this;
	}

	/**
	 * Moves one step forward in the animation if this is possible
	 */
	public Trace forward() {
		if (canGoForward()) {
			TraceElement p = head;
			while (!p.getPrevious().equals(current)) {
				p = p.getPrevious();
			}
			return new Trace(stateSpace, head, p, transitionList, this.uuid);
		}
		return this;
	}

	public Trace gotoPosition(int pos) {
		Trace trace = this;
		int currentIndex = trace.getCurrent().getIndex();
		if (pos == currentIndex) {
			return trace;
		} else if (pos > currentIndex && pos < size()) {
			while (pos != trace.getCurrent().getIndex()) {
				trace = trace.forward();
			}
		} else if (pos < currentIndex && pos >= -1) {
			while (pos != trace.getCurrent().getIndex()) {
				trace = trace.back();
			}
		}
		return trace;
	}

	public boolean canGoForward() {
		return !current.equals(head);
	}

	public boolean canGoBack() {
		return current.getPrevious() != null;
	}

	private PersistentVector<Transition> branchTransitionListIfNecessary(Transition newOp) {
		if (head.equals(current)) {
			return transitionList.assocN(transitionList.size(), newOp);
		} else {
			final PersistentVector<Transition> tList = PersistentVector.create(transitionList.subList(0, current.getIndex() + 1));
			return tList.assocN(tList.size(), newOp);
		}
	}

	@Override
	public String toString() {
		return stateSpace.printOps(current.getCurrentState()) + getRep();
	}

	public String getRep() {
		if (current.getTransition() == null) {
			return "";
		}
		return getCurrent().getIndex() + " previous transitions. Last executed transition: " + getCurrent().getTransition().evaluate(FormulaExpand.TRUNCATE).getRep();
	}

	public Trace randomAnimation(final int numOfSteps) {
		if (numOfSteps <= 0) {
			return this;
		}

		State currentState = this.current.getCurrentState();
		TraceElement current = this.current;
		PersistentVector<Transition> transitionList = this.transitionList;
		try {
			this.stateSpace.startTransaction();
			for (int i = 0; i < numOfSteps; i++) {
				final List<Transition> ops = currentState.getOutTransitions();
				if (ops.isEmpty()) {
					break;
				}
				Collections.shuffle(ops);
				final Transition op = ops.get(0);
				current = new TraceElement(op, current);
				if (i == 0) {
					transitionList = branchTransitionListIfNecessary(op);
				} else {
					transitionList = transitionList.assocN(transitionList.size(), op);
				}
				currentState = op.getDestination();
				if(Thread.currentThread().isInterrupted()) {
					return this;
				}
			}
		} finally {
			this.stateSpace.endTransaction();
		}

		return new Trace(stateSpace, current, transitionList, this.uuid);
	}

	/**
	 * Takes an event name and a list of String predicates and uses {@link State#findTransition(String, List)}
	 * with the {@link Trace#getCurrentState()}, the specified event name, and the conjunction of the parameters.
	 * If the specified operation is invalid, a runtime exception will be thrown.
	 *
	 * @param event String event name
	 * @param predicates List of String predicates to be conjoined
	 * @return {@link Trace} which is a result of executing the specified operation
	 */
	public Trace execute(String event, List<String> predicates) {
		final Transition transition = getCurrentState().findTransition(event, predicates);
		if (transition == null) {
			throw new IllegalArgumentException("Could not execute event with name " + event + " and parameters " + predicates);
		}
		return add(transition);
	}

	public Trace execute(String event, String... predicates) {
		return execute(event, Arrays.asList(predicates));
	}

	/**
	 * Tests to see if the event name plus the conjunction of the parameter strings produce a valid
	 * operation on this state. Uses implementation in {@link Trace#canExecuteEvent(String, List)}
	 *
	 * @param event Name of the event to be executed
	 * @param predicates to be conjoined
	 * @return {@code true}, if the operation can be executed. {@code false}, otherwise
	 */
	public boolean canExecuteEvent(String event, String... predicates) {
		return canExecuteEvent(event, Arrays.asList(predicates));
	}

	/**
	 * Tests to see if the event name plus the conjunction of the parameter strings produce a valid
	 * operation on this state. Uses implementation in {@link StateSpace#isValidOperation(State, String, String)}
	 *
	 * @param event Name of the event to be executed
	 * @param predicates List of String predicates to be conjoined
	 * @return {@code true}, if the operation can be executed. {@code false}, otherwise
	 */
	public boolean canExecuteEvent(String event, List<String> predicates) {
		try {
			Transition t = getCurrentState().findTransition(event, predicates);
			return t != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	// TODO This duplicates State.anyOperation (almost, but not exactly)
	public Trace anyOperation(final Object filter) {
		List<Transition> ops = current.getCurrentState().getOutTransitions();
		if (filter instanceof String) {
			final Pattern filterPattern = Pattern.compile((String)filter);
			ops = ops.stream().filter(t -> filterPattern.matcher(t.getName()).matches()).collect(Collectors.toList());
		}
		if (filter instanceof ArrayList) {
			ops = ops.stream().filter(t -> ((List<?>)filter).contains(t.getName())).collect(Collectors.toList());
		}
		Collections.shuffle(ops);
		if (!ops.isEmpty()) {
			Transition op = ops.get(0);
			return add(op.getId());
		}
		return this;
	}

	public Trace anyEvent(Object filter) {
		return anyOperation(filter);
	}

	public StateSpace getStateSpace() {
		return stateSpace;
	}

	public Set<Transition> getNextTransitions() {
		return new LinkedHashSet<>(getCurrentState().getOutTransitions());
	}

	/**
	 * @deprecated Use {@link #getNextTransitions()} instead.
	 *     If {@code evaluate} was set to {@code true},
	 *     also call {@link StateSpace#evaluateTransitions(Collection, EvalOptions)} on the returned set.
	 */
	@Deprecated
	public Set<Transition> getNextTransitions(boolean evaluate) {
		return this.getNextTransitions(evaluate, FormulaExpand.TRUNCATE);
	}

	/**
	 * @deprecated Use {@link #getNextTransitions()} instead.
	 *     If {@code evaluate} was set to {@code true},
	 *     also call {@link StateSpace#evaluateTransitions(Collection, FormulaExpand)} on the returned set.
	 */
	@Deprecated
	public Set<Transition> getNextTransitions(boolean evaluate, FormulaExpand expansion) {
		return new LinkedHashSet<>(getCurrentState().getOutTransitions(evaluate, expansion));
	}

	public State getCurrentState() {
		return current.getCurrentState();
	}

	public State getPreviousState() {
		if(!canGoBack()) {
			return null;
		}
		return current.getPrevious().getCurrentState();
	}

	public Transition getCurrentTransition() {
		return current.getTransition();
	}

	public AbstractModel getModel() {
		return stateSpace.getModel();
	}

	public Object asType(Class<?> clazz) {
		if (clazz == StateSpace.class) {
			return stateSpace;
		}
		if (clazz == AbstractModel.class) {
			return stateSpace.getModel();
		}
		if (clazz == stateSpace.getModel().getClass()) {
			return stateSpace.getModel();
		}
		throw new ClassCastException("Not able to convert Trace object to " + clazz);
	}

	public List<Transition> getTransitionList() {
		return transitionList;
	}

	/**
	 * @deprecated Use {@link #getTransitionList()} instead.
	 *     If {@code evaluate} was set to {@code true},
	 *     also call {@link StateSpace#evaluateTransitions(Collection, FormulaExpand)} on the returned list.
	 */
	@Deprecated
	public List<Transition> getTransitionList(boolean evaluate) {
		return this.getTransitionList(evaluate, FormulaExpand.TRUNCATE);
	}

	/**
	 * @deprecated Use {@link #getTransitionList()} instead.
	 *     If {@code evaluate} was set to {@code true},
	 *     also call {@link StateSpace#evaluateTransitions(Collection, FormulaExpand)} on the returned list.
	 */
	@Deprecated
	public List<Transition> getTransitionList(boolean evaluate, FormulaExpand expansion) {
		final List<Transition> ops = transitionList;
		if (evaluate) {
			stateSpace.evaluateTransitions(ops, expansion);
		}
		return ops;
	}

	/**
	 * Takes a {@link StateSpace} and a list of {@link Transition} operations through the {@link StateSpace}
	 * and generates a {@link Trace} object from the information. The list of operations must be a valid
	 * list of operations starting from the root state, and for which the information has already been
	 * cached in the {@link StateSpace}. Otherwise, an assertion error will be thrown. Calls {@link Trace#addTransitions(List)}
	 *
	 * @param s {@link StateSpace} through which the Trace should be generated
	 * @param ops List of {@link Transition} operations that should be executed in the Trace
	 * @return {@link Trace} specified by list of operations
	 */
	public static Trace getTraceFromTransitions(StateSpace s, List<Transition> ops) {
		if (!ops.isEmpty()) {
			Trace t = new Trace(ops.get(0).getSource());
			return t.addTransitions(ops);
		}
		return new Trace(s);
	}

	/**
	 * Adds a list of operations to an existing trace.
	 *
	 * @param ops List of {@link Transition} objects that should be added to the current trace
	 * @return Trace with the ops added
	 */
	public Trace addTransitions(List<Transition> ops) {
		if (ops.isEmpty()) {
			return this;
		}

		final Transition first = ops.get(0);
		TraceElement h = new TraceElement(first, current);
		PersistentVector<Transition> transitionList = branchTransitionListIfNecessary(first);
		for (Transition op : ops.subList(1, ops.size())) {
			h = new TraceElement(op, h);
			transitionList = transitionList.assocN(transitionList.size(), op);
		}
		return new Trace(stateSpace, h, transitionList, this.uuid);
	}

	/**
	 * @return an identical Trace object with a different UUID
	 */
	public Trace copy() {
		return new Trace(stateSpace, head, current, transitionList, UUID.randomUUID());
	}
}
