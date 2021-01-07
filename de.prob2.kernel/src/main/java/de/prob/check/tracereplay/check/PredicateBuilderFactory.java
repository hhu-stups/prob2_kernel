package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.formula.PredicateBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class PredicateBuilderFactory implements PredicateBuilderFactoryInterface {

	private final boolean variablesEnabled;
	private final boolean parametersEnabled;
	private final boolean outputEnabled;
	private final Set<String> blackListedVariables;


	public PredicateBuilderFactory(boolean variablesEnabled, boolean parametersEnabled, boolean outputEnabled, Set<String> blackListedVariables){
		this.variablesEnabled = variablesEnabled;
		this.parametersEnabled = parametersEnabled;
		this.outputEnabled = outputEnabled;
		this.blackListedVariables = blackListedVariables;
	}

	public PredicateBuilderFactory(){
		this.variablesEnabled = true;
		this.parametersEnabled = true;
		this.outputEnabled = true;
		this.blackListedVariables = Collections.emptySet();
	}

	@Override
	public PredicateBuilder createPredicateBuilder(PersistentTransition transition) {
		PredicateBuilder predicateBuilder = new PredicateBuilder();
		if(parametersEnabled){
			predicateBuilder.addMap(transition.getParameters());
		}
		if(outputEnabled){
			predicateBuilder.addMap(transition.getOutputParameters());
		}

		Map<String, String> changed = transition.getDestinationStateVariables()
				.entrySet()
				.stream()
				.filter(entry -> !blackListedVariables.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		if(variablesEnabled){
			predicateBuilder.addMap(changed);
		}
		return predicateBuilder;
	}
}
