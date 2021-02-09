package de.prob.check.tracereplay.check;

import de.prob.statespace.OperationInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

public class Delta {

	final String originalName;
	final String deltaName;
	final Map<String, String> inputParameters;
	final Map<String, String> outputParameters;
	final Map<String, String> variables;

	/**
	 * Extracts tbe delta from the calculated changes
	 * @param changes the changes calculated by the Delta Finder
	 * @param operationInfo the old operation
	 */
	public Delta(Map<String, String> changes, OperationInfo operationInfo){
		originalName = operationInfo.getOperationName();
		List<String> oldInput = operationInfo.getParameterNames();
		List<String> oldOutput = operationInfo.getOutputParameterNames();
		List<String> oldVariables = operationInfo.getAllVariables();


		inputParameters = changes.entrySet().stream()
				.filter(stringStringEntry -> oldInput.contains(stringStringEntry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		outputParameters = changes.entrySet().stream()
				.filter(stringStringEntry -> oldOutput.contains(stringStringEntry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		variables = changes.entrySet().stream()
				.filter(stringStringEntry -> oldVariables.contains(stringStringEntry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		deltaName = changes.get(originalName);

	}

	/**
	 * Like the other constructor - the candidates are splitted
	 * @param originalName the name of the operation in the old machine
	 * @param deltaName the new name
	 * @param inputParameters a map, mapping new to old input variables
	 * @param outputParameters a map, mapping new to old output variables
	 * @param variables a map, mapping new to old variables
	 */
	public Delta(String originalName, String deltaName, Map<String, String> inputParameters, Map<String, String> outputParameters,
				 Map<String, String> variables){
		this.originalName = originalName;
		this.deltaName = deltaName;
		this.inputParameters = inputParameters;
		this.outputParameters = outputParameters;
		this.variables = variables;
	}

	/**
	 * Constructor for initialisation
	 * @param name
	 * @param variables
	 */
	public Delta(String name, Map<String, String> variables){
		this.originalName = name;
		this.deltaName = name;
		this.inputParameters = emptyMap();
		this.outputParameters = emptyMap();
		this.variables = variables;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getDeltaName() {
		return deltaName;
	}

	public Map<String, String> getInputParameters() {
		return inputParameters;
	}

	public Map<String, String> getOutputParameters() {
		return outputParameters;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	@Override
	public String toString() {
		return "Original Name <" + originalName + "> Delta Name <" + deltaName+ "> Output Parameter: " +
				outputParameters + " Input Parameter: " + inputParameters + " Variables " + variables;
	}
}
