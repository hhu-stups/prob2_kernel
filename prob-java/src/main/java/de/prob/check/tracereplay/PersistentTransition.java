package de.prob.check.tracereplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalExpandMode;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.FormulaTranslationMode;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.EvaluatedTransitionInfo;
import de.prob.statespace.Language;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.State;
import de.prob.statespace.Transition;

import static java.util.Collections.*;

@JsonPropertyOrder({"name", "params", "results", "destState", "destStateNotChanged", "preds", "postconditions", "description"})
public class PersistentTransition {
	private static final EvalOptions TRACE_SAVE_EVAL_OPTIONS = EvalOptions.DEFAULT
		.withEvalExpand(EvalExpandMode.EFFICIENT)
		.withExpand(FormulaExpand.EXPAND)
		.withMode(FormulaTranslationMode.UNICODE)
		// TODO Support formalisms that are not B or translated to B
		.withLanguage(Language.CLASSICAL_B);

	private final String name;

	private final Map<String, String> params = new HashMap<>();
	private final Map<String, String> results = new HashMap<>();
	private final Map<String, String> destState = new HashMap<>();
	private final Set<String> destStateNotChanged = new HashSet<>();
	private final List<String> preds = new ArrayList<>();
	private final List<Postcondition> postconditions = new ArrayList<>();
	private String description = "";


	// FIXME Why is the default value of storeDestinationState false for constructor with 1 argument?
	public PersistentTransition(Transition transition) {
		this(transition, false, null);
	}

	// FIXME Why is the following constructor duplicated? What's the difference between the two variants?

	public PersistentTransition(Transition transition, boolean storeDestinationState, PersistentTransition transitionAfter) {
		this.name = transition.getName();
		final LoadedMachine loadedMachine = transition.getStateSpace().getLoadedMachine();
		final State destinationState = transition.getDestination();
		if (Transition.SETUP_CONSTANTS_NAME.equals(name)) {
			if (storeDestinationState) {
				addValuesToDestState(destinationState.getConstantValues(TRACE_SAVE_EVAL_OPTIONS), null);
			}
		} else {
			if (storeDestinationState) {
				addValuesToDestState(destinationState.getVariableValues(TRACE_SAVE_EVAL_OPTIONS), transitionAfter
				);
			}

			if (!Transition.INITIALISE_MACHINE_NAME.equals(name)) {
				// for each operation
				OperationInfo machineOperationInfo = loadedMachine.getMachineOperationInfo(name);
				final EvaluatedTransitionInfo evaluated = transition.evaluate(TRACE_SAVE_EVAL_OPTIONS);

				for (int i = 0; i < machineOperationInfo.getParameterNames().size(); i++) {
					params.put(machineOperationInfo.getParameterNames().get(i), evaluated.getParameterValues().get(i));
				}

				for (int i = 0; i < machineOperationInfo.getOutputParameterNames().size(); i++) {
					results.put(machineOperationInfo.getOutputParameterNames().get(i),
							evaluated.getReturnValues().get(i));
				}
			}
		}
	}


	// FIXME While for 2 arguments, the default value of storeDestinationState is true
	public PersistentTransition(Transition transition, PersistentTransition transitionBefore) {
		this.name = transition.getName();
		final LoadedMachine loadedMachine = transition.getStateSpace().getLoadedMachine();
		final State destinationState = transition.getDestination();
		if (Transition.SETUP_CONSTANTS_NAME.equals(name) ) {
			addValuesToDestState2(destinationState.getConstantValues(TRACE_SAVE_EVAL_OPTIONS), null);
		} else {

			addValuesToDestState2(destinationState.getVariableValues(TRACE_SAVE_EVAL_OPTIONS), transitionBefore);


			if (!Transition.INITIALISE_MACHINE_NAME.equals(name)) {
				// for each operation
				OperationInfo machineOperationInfo = loadedMachine.getMachineOperationInfo(name);
				final EvaluatedTransitionInfo evaluated = transition.evaluate(TRACE_SAVE_EVAL_OPTIONS);

				for (int i = 0; i < machineOperationInfo.getParameterNames().size(); i++) {
					params.put(machineOperationInfo.getParameterNames().get(i), evaluated.getParameterValues().get(i));
				}

				for (int i = 0; i < machineOperationInfo.getOutputParameterNames().size(); i++) {
					results.put(machineOperationInfo.getOutputParameterNames().get(i),
							evaluated.getReturnValues().get(i));
				}
			}
		}
	}


	/**
	 * Jackson constructor, only called by jackson deserializer
	 * @param name the name of the transition
	 * @param params parameters of the transition
	 * @param results result of the transition
	 * @param destState target state of the transition
	 * @param destStateNotChanged target state is no change
	 * @param preds predicates
	 * @param postconditions postconditions
	 */
	public PersistentTransition(@JsonProperty("name") String name,
								@JsonProperty("params") Map<String, String> params,
								@JsonProperty("results") Map<String, String> results,
								@JsonProperty("destState") Map<String, String> destState,
								@JsonProperty("destStateNotChanged") Set<String> destStateNotChanged,
								@JsonProperty("preds") List<String> preds,
								@JsonProperty("postconditions") List<Postcondition> postconditions,
								@JsonProperty("description") String description){
		this.name = Objects.requireNonNull(name, "name");
		this.params.putAll(Objects.requireNonNull(params, "params"));
		this.results.putAll(Objects.requireNonNull(results, "results"));
		this.destState.putAll(Objects.requireNonNull(destState, "destState"));
		this.destStateNotChanged.addAll(Objects.requireNonNull(destStateNotChanged, "destStateNotChanged"));
		this.preds.addAll(Objects.requireNonNull(preds, "preds"));
		this.postconditions.addAll(Objects.requireNonNull(postconditions, "postconditions"));
		this.description = Objects.requireNonNull(description, "description");
	}


	public PersistentTransition(String name) {
		this.name = name;
	}

	public static List<PersistentTransition> createFromList(final List<Transition> transitions){
		if(transitions.isEmpty()) return emptyList();
		PersistentTransition first = new PersistentTransition(transitions.get(0), null);
		transitions.remove(0);
		List<PersistentTransition> result = new ArrayList<>();
		result.add(first);
		for(int i= 0; i < transitions.size(); i++){
			result.add(new PersistentTransition(transitions.get(i), result.get(i)));
		}
		return result;

	}

	public static List<PersistentTransition> createFromList(final List<Transition> transitions, Transition before){
		if(transitions.isEmpty()) return emptyList();
		PersistentTransition first = new PersistentTransition(transitions.get(0), new PersistentTransition(before));
		transitions.remove(0);
		List<PersistentTransition> result = new ArrayList<>();
		result.add(first);
		for(int i= 0; i < transitions.size(); i++){
			result.add(new PersistentTransition(transitions.get(i), result.get(i)));
		}
		return result;

	}

	public static List<PersistentTransition> createFromList(final List<Transition> transitions, PersistentTransition before){
		if(transitions.isEmpty()) return emptyList();
		PersistentTransition first = new PersistentTransition(transitions.get(0), before);
		transitions.remove(0);
		List<PersistentTransition> result = new ArrayList<>();
		result.add(first);
		for(int i= 0; i < transitions.size(); i++){
			result.add(new PersistentTransition(transitions.get(i), result.get(i)));
		}
		return result;

	}

	public PersistentTransition copyWithNewName(String name){
		return new PersistentTransition(name, params, results, destState, destStateNotChanged, preds, postconditions, description);
	}

	public PersistentTransition copyWithNewDestState(Map<String, String> destState){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds, postconditions, description);
	}

	public PersistentTransition copyWithNewParameters(Map<String, String> params){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds, postconditions, description);
	}

	public PersistentTransition copyWithNewOutputParameters(Map<String, String> outputParameters){
		return  new PersistentTransition(name, params, outputParameters, destState, destStateNotChanged, preds, postconditions, description);
	}

	public PersistentTransition copyWithDestStateNotChanged(Set<String> destStateNotChanged){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds, postconditions, description);
	}

	public PersistentTransition copyWithNewPostconditions(List<Postcondition> postconditions){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds, postconditions, description);
	}

	public PersistentTransition copyWithNewDescription(String description){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds, postconditions, description);
	}

	private void addValuesToDestState2(Map<IEvalElement, AbstractEvalResult> map,  PersistentTransition transitionBefore) {

		for (Map.Entry<IEvalElement, AbstractEvalResult> entry : map.entrySet()) {
			if (entry.getValue() instanceof EvalResult) {
				String name = entry.getKey().getCode();
				String value = ((EvalResult) entry.getValue()).getValue();
				destState.put(name, value);
				if(transitionBefore != null && value.equals(transitionBefore.destState.get(name))) {
					destState.remove(name);
					destStateNotChanged.add(name);
				}
			}
		}
	}


	private void addValuesToDestState(Map<IEvalElement, AbstractEvalResult> map, PersistentTransition transitionAfter) {

		for (Map.Entry<IEvalElement, AbstractEvalResult> entry : map.entrySet()) {
			if (entry.getValue() instanceof EvalResult) {
				String name = entry.getKey().getCode();
				String value = ((EvalResult) entry.getValue()).getValue();
				destState.put(name, value);
				//If the future is the same as the current state, then the current state can't change...
				if(transitionAfter != null && value.equals(transitionAfter.destState.get(name))) {
					transitionAfter.destState.remove(name);
					transitionAfter.destStateNotChanged.add(name);
				}
			}
		}
	}

	@JsonProperty("name")
	public String getOperationName() {
		return name;
	}


	@JsonProperty("destStateNotChanged")
	public Set<String> getDestStateNotChanged() {
		if(destStateNotChanged==null){
			return emptySet();
		}
		return new HashSet<>(this.destStateNotChanged);


	}

	@JsonProperty("preds")
	public List<String> getAdditionalPredicates() {
		if(preds == null){
			return emptyList();
		}
		return new ArrayList<>(this.preds);
	}

	@JsonProperty("params")
	public Map<String, String> getParameters() {
		if(params == null){
			return emptyMap();
		}
		return new HashMap<>(this.params);
	}


	@JsonProperty("results")
	public Map<String, String> getOutputParameters() {
		if(results == null){
			return emptyMap();
		}
		return new HashMap<>(results);
	}


	@JsonProperty("destState")
	public Map<String, String> getDestinationStateVariables() {

		if(destState == null){
			return emptyMap();
		}

		return new HashMap<>(this.destState);
	}

	@JsonProperty("postconditions")
	public List<Postcondition> getPostconditions() {
		if(postconditions == null){
			return new ArrayList<>();
		}
		// Do not invoke ArrayList constructor as it will copy the list
		return this.postconditions;
	}

	@JsonProperty("description")
	public String getDescription() {
		if(description == null) {
			return "";
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonIgnore
	public Map<String, String> getAllPredicates(){
		Map<String, String> result = new HashMap<>();
		result.putAll(getDestinationStateVariables());
		result.putAll(getOutputParameters());
		result.putAll(getParameters());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		PersistentTransition other = (PersistentTransition)obj;
		return this.name.equals(other.name)
			&& this.params.equals(other.params)
			&& this.results.equals(other.results)
			&& this.destState.equals(other.destState)
			&& this.destStateNotChanged.equals(other.destStateNotChanged)
			&& this.preds.equals(other.preds)
			&& this.postconditions.equals(other.postconditions)
			&& this.description.equals(other.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			this.name,
			this.params,
			this.results,
			this.destState,
			this.destStateNotChanged,
			this.preds,
			this.postconditions,
			this.description
		);
	}

	@Override
	public String toString() {
		return "PersistentTransition{" +
				"name='" + name + '\'' +
				", params=" + params +
				", results=" + results +
				", destState=" + destState +
				", destStateNotChanged=" + destStateNotChanged +
				", preds=" + preds +
				", postconditions=" + postconditions +
				", description=" + description +
				'}';
	}
}
