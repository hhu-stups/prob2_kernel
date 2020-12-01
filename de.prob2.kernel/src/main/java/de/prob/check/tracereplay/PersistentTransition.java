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

public class PersistentTransition {

	private final String name;
	private final Map<String, String> params = new HashMap<>();
	private final Map<String, String> outputParameters = new HashMap<>();
	private final Map<String, String> destState = new HashMap<>();
	private final Set<String> destStateNotChanged = new HashSet<>();
	private final List<String> additionalPredicates = new ArrayList<>();

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
					outputParameters.put(machineOperationInfo.getOutputParameterNames().get(i),
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
					outputParameters.put(machineOperationInfo.getOutputParameterNames().get(i),
							transition.getReturnValues().get(i));
				}
			}
		}
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
			additionalPredicates = Collections.emptyList();
		}

		this.name = name;
		this.params.putAll(params);
		this.outputParameters.putAll(outputParameters);
		this.destState.putAll(destState);
		this.destStateNotChanged.addAll(destStateNotChanged);
		this.additionalPredicates.addAll(additionalPredicates);

	}


	public PersistentTransition createFromOld(Map<String, String> destState){
		return  new PersistentTransition(name, params, outputParameters, destState, destStateNotChanged, additionalPredicates);
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
		return new HashSet<>(this.destStateNotChanged);
	}

	public List<String> getAdditionalPredicates() {
		return new ArrayList<>(this.additionalPredicates);
	}

	public Map<String, String> getParameters() {
		return new HashMap<>(this.params);
	}


	public Map<String, String> getOutputParameters() {
		return outputParameters;
	}

	public Map<String, String> getDestinationStateVariables() {
		return new HashMap<>(this.destState);
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PersistentTransition)
		{
			if(((PersistentTransition) obj).name.equals(this.name)){
				if(((PersistentTransition) obj).params.equals(this.params)){
					if(((PersistentTransition) obj).destState.equals(this.destState)){
						if(((PersistentTransition) obj).destStateNotChanged.equals(this.destStateNotChanged)){
							if(((PersistentTransition) obj).outputParameters.equals(this.outputParameters)){
								return ((PersistentTransition) obj).additionalPredicates.equals(this.additionalPredicates);
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
				+ outputParameters.toString()  +destStateNotChanged.toString() ;
	}
}
