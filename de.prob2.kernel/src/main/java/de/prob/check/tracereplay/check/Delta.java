package de.prob.check.tracereplay.check;

import de.prob.statespace.OperationInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Delta {

	final String originalName;
	final String deltaName;
	final Map<String, String> inputParameters;
	final Map<String, String> outputParameters;
	final Map<String, String> variables;


	public Delta(Map<String, String> changes, OperationInfo operationInfo){
		originalName = operationInfo.getOperationName();
		List<String> oldInput = operationInfo.getParameterNames();
		List<String> oldOutput = operationInfo.getOutputParameterNames();

		inputParameters = changes.entrySet().stream()
				.filter(stringStringEntry -> oldInput.contains(stringStringEntry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		outputParameters = changes.entrySet().stream()
				.filter(stringStringEntry -> oldOutput.contains(stringStringEntry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		List<String> oldVariables = operationInfo.getReadVariables();
		oldVariables.addAll(operationInfo.getWrittenVariables());
		oldVariables.addAll(operationInfo.getNonDetWrittenVariables());

		variables = changes.entrySet().stream()
				.filter(stringStringEntry -> oldVariables.contains(stringStringEntry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		deltaName = changes.entrySet().stream().filter(stringStringEntry ->
						!oldVariables.contains(stringStringEntry.getKey()) &&
								!oldInput.contains(stringStringEntry.getKey()) &&
								!oldOutput.contains(stringStringEntry.getKey()))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList()).get(0);

	}

	public Delta(String originalName, String deltaName, Map<String, String> inputParameters, Map<String, String> outputParameters,
				 Map<String, String> variables){
		this.originalName = originalName;
		this.deltaName = deltaName;
		this.inputParameters = inputParameters;
		this.outputParameters = outputParameters;
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
