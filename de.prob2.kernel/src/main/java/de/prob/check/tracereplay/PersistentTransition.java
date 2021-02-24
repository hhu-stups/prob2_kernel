package de.prob.check.tracereplay;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.State;
import de.prob.statespace.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.*;

public class PersistentTransition {

	private final String name;
	private final Map<String, String> results = new HashMap<>();
	private final List<String> preds = new ArrayList<>();

	private final Map<String, String> params = new HashMap<>();
	//private final Map<String, String> results = new HashMap<>();
	private final Map<String, String> destState = new HashMap<>();
	private final Set<String> destStateNotChanged = new HashSet<>();
	//private final List<String> preds = new ArrayList<>();


	public PersistentTransition(Transition transition) {
		this(transition, false, null);
	}

	public PersistentTransition(Transition transition, boolean storeDestinationState, PersistentTransition transitionAfter) {
		this.name = transition.getName();
		final LoadedMachine loadedMachine = transition.getStateSpace().getLoadedMachine();
		final State destinationState = transition.getDestination();
		if (Transition.SETUP_CONSTANTS_NAME.equals(name)) {
			if (storeDestinationState) {
				addValuesToDestState(destinationState.getConstantValues(FormulaExpand.EXPAND), null);
			}
		} else {
			if (storeDestinationState) {
				addValuesToDestState(destinationState.getVariableValues(FormulaExpand.EXPAND), transitionAfter
				);
			}

			if (!Transition.INITIALISE_MACHINE_NAME.equals(name)) {
				// for each operation
				OperationInfo machineOperationInfo = loadedMachine.getMachineOperationInfo(name);

				for (int i = 0; i < machineOperationInfo.getParameterNames().size(); i++) {
					params.put(machineOperationInfo.getParameterNames().get(i), transition.getParameterValues().get(i));
				}

				for (int i = 0; i < machineOperationInfo.getOutputParameterNames().size(); i++) {
					results.put(machineOperationInfo.getOutputParameterNames().get(i),
							transition.getReturnValues().get(i));
				}
			}
		}
	}


	public PersistentTransition(Transition transition, PersistentTransition transitionBefore) {
		this.name = transition.getName();
		final LoadedMachine loadedMachine = transition.getStateSpace().getLoadedMachine();
		final State destinationState = transition.getDestination();
		if (Transition.SETUP_CONSTANTS_NAME.equals(name) ) {
			addValuesToDestState2(destinationState.getConstantValues(FormulaExpand.EXPAND), null);
		} else {

			addValuesToDestState2(destinationState.getVariableValues(FormulaExpand.EXPAND), transitionBefore);


			if (!Transition.INITIALISE_MACHINE_NAME.equals(name)) {
				// for each operation
				OperationInfo machineOperationInfo = loadedMachine.getMachineOperationInfo(name);

				for (int i = 0; i < machineOperationInfo.getParameterNames().size(); i++) {
					params.put(machineOperationInfo.getParameterNames().get(i), transition.getParameterValues().get(i));
				}

				for (int i = 0; i < machineOperationInfo.getOutputParameterNames().size(); i++) {
					results.put(machineOperationInfo.getOutputParameterNames().get(i),
							transition.getReturnValues().get(i));
				}
			}
		}
	}


	public void addUnchangedValues(){

	}

	/**
	 * Jackson constructor, only called by jackson deserializer
	 * @param name the name of the transition
	 * @param params parameters of the transition
	 * @param outputParameters result of the transition
	 * @param destState target state of the transition
	 * @param destStateNotChanged target state is no change
	 * @param additionalPredicates predicates
	 */
	public PersistentTransition(@JsonProperty("operationName") String name,
								@JsonProperty("parameters") Map<String, String> params,
								@JsonProperty("outputParameters") Map<String, String> outputParameters,
								@JsonProperty("destinationStateVariables") Map<String, String> destState,
								@JsonProperty("destStateNotChanged") Set<String> destStateNotChanged,
								@JsonProperty("additionalPredicates") List<String> additionalPredicates){
		if(name.equals(Transition.INITIALISE_MACHINE_NAME)){
			params = Collections.emptyMap();
			outputParameters = Collections.emptyMap();
			name = Transition.INITIALISE_MACHINE_NAME;
		}

		if(additionalPredicates==null){
			additionalPredicates = emptyList();
		}

		this.name = name;
		this.params.putAll(params);
		this.results.putAll(outputParameters);
		this.destState.putAll(destState);
		this.destStateNotChanged.addAll(destStateNotChanged);
		this.preds.addAll(additionalPredicates);

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


	public PersistentTransition copyWithNewDestState(Map<String, String> destState){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds);
	}

	public PersistentTransition copyWithNewParameters(Map<String, String> params){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds);
	}

	public PersistentTransition copyWithNewOutputParameters(Map<String, String> outputParameters){
		return  new PersistentTransition(name, params, outputParameters, destState, destStateNotChanged, preds);
	}

	public PersistentTransition copyWithDestStateNotChanged(Set<String> destStateNotChanged){
		return  new PersistentTransition(name, params, results, destState, destStateNotChanged, preds);
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

	public String getOperationName() {
		return name;
	}


	public Set<String> getDestStateNotChanged() {
		if(destStateNotChanged==null){
			return emptySet();
		}
		return new HashSet<>(this.destStateNotChanged);


	}

	public List<String> getAdditionalPredicates() {
		if(preds == null){
			return emptyList();
		}
		return new ArrayList<>(this.preds);
	}

	public Map<String, String> getParameters() {
		if(params == null){
			return emptyMap();
		}
		return new HashMap<>(this.params);
	}


	public Map<String, String> getOutputParameters() {
		if(results == null){
			return emptyMap();
		}
		return new HashMap<>(results);
	}

	public Map<String, String> getDestinationStateVariables() {

		if(destState == null){
			return emptyMap();
		}

		return new HashMap<>(this.destState);
	}


	public Map<String, String> getAllPredicates(){
		Map<String, String> result = new HashMap<>();
		result.putAll(getDestinationStateVariables());
		result.putAll(getOutputParameters());
		result.putAll(getParameters());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PersistentTransition)
		{
			if(((PersistentTransition) obj).name.equals(this.name)){
				if(((PersistentTransition) obj).params.equals(this.params)){
					if(((PersistentTransition) obj).destState.equals(this.destState)){
						if(((PersistentTransition) obj).destStateNotChanged.equals(this.destStateNotChanged)){
							if(((PersistentTransition) obj).results.equals(this.results)){
								return ((PersistentTransition) obj).preds.equals(this.preds);
							}
						}
					}
				}
			}
		}
		return false;

	}


	@Override
	public String toString() {
		return "PersistentTransition:" + name +  params.toString()  + destState.toString()
				+ results.toString()  +destStateNotChanged.toString() ;
	}
}
