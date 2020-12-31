package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.formula.PredicateBuilder;

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
