package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.formula.PredicateBuilder;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class PrivilegeLevel{

	final boolean name;
	final boolean variables;
	final boolean inputParameters;
	final boolean outputParameter;


	public PrivilegeLevel(PersistentTransition transition){
		this.name = true;
		this.inputParameters = !transition.getParameters().isEmpty();
		this.variables = !transition.getDestinationStateVariables().isEmpty();
		this.outputParameter = !transition.getOutputParameters().isEmpty();
	}

	public PrivilegeLevel(boolean name, boolean variables, boolean inputParameters, boolean outputParameter){
		this.name = name;
		this.inputParameters = inputParameters;
		this.variables = variables;
		this.outputParameter = outputParameter;
	}

	public PrivilegeLevel(){
		this.name = true;
		this.inputParameters = true;
		this.variables = true;
		this.outputParameter = true;
	}

	public  boolean isOff(){
		return variables&&inputParameters&&outputParameter;
	}

	public PrivilegeLevel dropName(){
		return new PrivilegeLevel(false, variables, inputParameters, outputParameter);
	}

	public PrivilegeLevel dropVariables(){
		return new PrivilegeLevel(name, false, inputParameters, outputParameter);
	}

	public PrivilegeLevel dropInput(){
		return new PrivilegeLevel(name, variables, false, outputParameter);
	}

	public PrivilegeLevel dropOutput(){
		return new PrivilegeLevel(name, variables, inputParameters, false);
	}

	public PrivilegeLevel dropEverythingExpectName(){
		return new PrivilegeLevel(name, false, false, false);
	}

	public List<PrivilegeLevel> downgrading(boolean ignoreName){
		if(inputParameters && outputParameter && variables){
			return Stream.of(dropInput(), dropOutput(), dropVariables()).collect(toList());
		}else if(inputParameters && outputParameter){
			return Stream.of(dropInput(), dropOutput()).collect(toList());
		} else if(inputParameters && variables){
			return Stream.of(dropInput(), dropVariables()).collect(toList());
		}else if(variables && outputParameter){
			return Stream.of(dropVariables(), dropOutput()).collect(toList());
		}else if(name && ignoreName){
			return Stream.of(dropEverythingExpectName()).collect(toList());
		}else{
			return emptyList();
		}
	}

	public PredicateBuilder constructPredicate(PersistentTransition transition){
		PredicateBuilder predicateBuilder = new PredicateBuilder();

		if(variables){
			predicateBuilder.addMap(transition.getDestinationStateVariables());
		}
		if(inputParameters){
			predicateBuilder.addMap(transition.getParameters());
		}
		if(outputParameter){
			predicateBuilder.addMap(transition.getOutputParameters());
		}

		return predicateBuilder;
	}

}
