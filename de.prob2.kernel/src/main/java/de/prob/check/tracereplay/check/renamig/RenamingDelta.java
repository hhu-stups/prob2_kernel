package de.prob.check.tracereplay.check.renamig;

import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.statespace.OperationInfo;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

public class RenamingDelta {

	private final String originalName;
	private final String deltaName;
	private final Map<String, String> inputParameters;
	private final Map<String, String> outputParameters;
	private final Map<String, String> variables;

	/**
	 * Extracts tbe delta from the calculated changes
	 * @param changes the changes calculated by the RenamingDelta Finder
	 * @param operationInfo the old operation
	 */
	public RenamingDelta(Map<String, String> changes, OperationInfo operationInfo){
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


	public RenamingDelta(String originalName, String deltaName, Map<TraceExplorer.MappingNames, Map<String, String>> mappings){
		this(originalName, deltaName, mappings.get(TraceExplorer.MappingNames.INPUT_PARAMETERS), mappings.get(TraceExplorer.MappingNames.OUTPUT_PARAMETERS),
				mappings.entrySet().stream()
				.filter(entry -> entry.getKey().equals(TraceExplorer.MappingNames.VARIABLES_MODIFIED) ||
						entry.getKey().equals(TraceExplorer.MappingNames.VARIABLES_READ))

				.flatMap(entry -> entry.getValue().entrySet().stream())
						.collect(Collectors.toSet())//through the split earlier there are entries with the same value, that would lead to a map execpetion, so we get rid of double elements
						.stream()
				.filter(new Predicate<Map.Entry<String, String>>() {
					final Set<String>  keySeen = new HashSet<>();
					@Override
					public boolean test(Map.Entry<String, String> stringStringEntry) {
						if(!keySeen.contains(stringStringEntry.getKey())){
							keySeen.add(stringStringEntry.getKey());
							return true;
						}
						return keySeen.add(stringStringEntry.getKey());

					}
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}



	/**
	 * Like the other constructor - the candidates are splitted
	 * @param originalName the name of the operation in the old machine
	 * @param deltaName the new name
	 * @param inputParameters a map, mapping new to old input variables
	 * @param outputParameters a map, mapping new to old output variables
	 * @param variables a map, mapping new to old variables
	 */
	public RenamingDelta(String originalName, String deltaName, Map<String, String> inputParameters, Map<String, String> outputParameters,
						 Map<String, String> variables){
		this.originalName = originalName;
		this.deltaName = deltaName;
		this.inputParameters = inputParameters;
		this.outputParameters = outputParameters;
		this.variables = variables;
	}

	/**
	 * Constructor for initialisation
	 * @param name name of init transition
	 * @param variables the variables initialised
	 */
	public RenamingDelta(String name, Map<String, String> variables){
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
		return "Original Name <" + originalName + "> RenamingDelta Name <" + deltaName+ "> Output Parameter: " +
				outputParameters + " Input Parameter: " + inputParameters + " Variables " + variables;
	}

	public boolean isPointless(){
		return originalName.equals(deltaName) && mapIsEqual(inputParameters) && mapIsEqual(outputParameters) && mapIsEqual(variables);
	}

	public static boolean mapIsEqual(Map<String, String> input){
		return input.entrySet().stream().allMatch(entry -> entry.getKey().equals(entry.getValue()));
	}
}
