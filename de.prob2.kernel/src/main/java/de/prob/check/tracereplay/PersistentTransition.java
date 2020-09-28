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
	private Map<String, String> params;
	private Map<String, String> results;
	private Map<String, String> destState;
	private Set<String> destStateNotChanged;
	private List<String> preds;


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
				params = new HashMap<>();
				for (int i = 0; i < machineOperationInfo.getParameterNames().size(); i++) {
					params.put(machineOperationInfo.getParameterNames().get(i), transition.getParameterValues().get(i));
				}
				results = new HashMap<>();
				for (int i = 0; i < machineOperationInfo.getOutputParameterNames().size(); i++) {
					results.put(machineOperationInfo.getOutputParameterNames().get(i),
							transition.getReturnValues().get(i));
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
	 */
	public PersistentTransition(@JsonProperty("name") String name,
								@JsonProperty("params") Map<String, String> params,
								@JsonProperty("results") Map<String, String> results,
								@JsonProperty("destState") Map<String, String> destState,
								@JsonProperty("destStateNotChanged") Set<String> destStateNotChanged,
								@JsonProperty("preds") List<String> preds){
		this.name = name;
		this.params = params;
		this.results = results;
		this.destState = destState;
		this.destStateNotChanged = destStateNotChanged;
		this.preds = preds;

	}

	private void addValuesToDestState(Map<IEvalElement, AbstractEvalResult> map, PersistentTransition transitionAfter) {
		if (destState == null) {
			destState = new HashMap<>();
			destStateNotChanged = new HashSet<>();
		}
		for (Map.Entry<IEvalElement, AbstractEvalResult> entry : map.entrySet()) {
			if (entry.getValue() instanceof EvalResult) {
				String name = entry.getKey().getCode();
				String value = ((EvalResult) entry.getValue()).getValue();
				destState.put(name, value);
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

	public Map<String, String> getParameters() {
		if (this.params == null) {
			return null;
		}
		return new HashMap<>(this.params);
	}

	public Map<String, String> getOutputParameters() {
		if (this.results == null) {
			return null;
		}
		return new HashMap<>(this.results);
	}

	public Map<String, String> getDestinationStateVariables() {
		if (this.destState == null) {
			return null;
		}
		return new HashMap<>(this.destState);
	}

	public List<String> getAdditionalPredicates() {
		if (this.preds == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(this.preds);
	}


}
