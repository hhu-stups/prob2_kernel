package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.json.HasMetadata;
import de.prob.json.JsonMetadata;
import de.prob.json.JsonMetadataBuilder;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Trace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Represents the trace file
 */
@JsonPropertyOrder({"description", "transitionList",  "variableNames", "constantNames", "setNames", "machineOperationInfos", "globalIdentifierTypes", "metadata"})
public class TraceJsonFile implements HasMetadata {
	public static final String FILE_TYPE = "Trace";
	public static final int CURRENT_FORMAT_VERSION = 2;

	private final String description;
	private final List<PersistentTransition> transitionList;
	private final List<String> variableNames;
	private final List<String> constantNames;
	private final List<String> setNames;
	private final Map<String, OperationInfo> machineOperationInfos;
	private final Map<String, OperationInfo> reducedMachineOperationInfos;
	private final Map<String, String> globalIdentifierTypes;
	private final JsonMetadata metadata;

	/**
	 * @param trace the trace to be stored
	 * @param metadata the metadata
	 */
	public TraceJsonFile(Trace trace, JsonMetadata metadata) {
		PersistentTrace persistentTrace = new PersistentTrace(trace);
		this.transitionList = persistentTrace.getTransitionList();
		this.description = persistentTrace.getDescription();
		variableNames = trace.getStateSpace().getLoadedMachine().getVariableNames();
		constantNames = trace.getStateSpace().getLoadedMachine().getConstantNames();
		setNames = trace.getStateSpace().getLoadedMachine().getSetNames();
		machineOperationInfos = trace.getStateSpace().getLoadedMachine().getOperations();
		globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
		this.metadata = metadata;
	}



	/**
	 *
	 * @param trace the trace to be stored
	 * @param machine the machine corresponding to the trace when it was created
	 * @param metadata the metadata
	 */
	public TraceJsonFile(PersistentTrace trace, LoadedMachine machine, JsonMetadata metadata) {
		this.transitionList = trace.getTransitionList();
		this.description = trace.getDescription();
		variableNames = machine.getVariableNames();
		constantNames = machine.getConstantNames();
		setNames = machine.getSetNames();
		machineOperationInfos = machine.getOperations();
		globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
		this.metadata = metadata;

	}


	/**
	 * The constructor to deserialize files
	 * @param description description
	 * @param trace trace
	 * @param variableNames variable names of the corresponding machine
	 * @param machineOperationInfos machine operation infos of the corresponding machine
	 * @param constantNames name of constants infos of the corresponding machine
	 * @param setNames name of sets operation infos of the corresponding machine
	 * @param metadata metadata
	 */
	public TraceJsonFile(@JsonProperty("description") String description,
						 @JsonProperty("trace") List<PersistentTransition> trace,
						 @JsonProperty("variablesNames") List<String> variableNames,
						 @JsonProperty("machineOperationInfos") Map<String, OperationInfo> machineOperationInfos,
						 @JsonProperty("constantNames") List<String> constantNames,
						 @JsonProperty("setNames") List<String> setNames,
						 @JsonProperty("globalIdentifierTypes") Map<String, String> globalIdentifierTypes,
						 @JsonProperty("metadata") JsonMetadata metadata) {


		this.transitionList = trace;
		this.description = description;
		this.variableNames = variableNames;
		this.constantNames = constantNames;
		this.setNames = setNames;
		this.reducedMachineOperationInfos = machineOperationInfos;
		this.globalIdentifierTypes = globalIdentifierTypes;
		this.machineOperationInfos = reassembleTypeInfo(globalIdentifierTypes, machineOperationInfos);
		this.metadata = metadata;

	}

	public TraceJsonFile(PersistentTrace trace, List<String> variableNames,
						   Map<String, OperationInfo> machineOperationInfos, List<String> constantNames, List<String> setNames,
						   JsonMetadata metadata) {


		this.transitionList = trace.getTransitionList();
		this.description = trace.getDescription();
		this.variableNames = variableNames;
		this.constantNames = constantNames;
		this.setNames = setNames;
		this.machineOperationInfos = machineOperationInfos;
		this.globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		this.reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
		this.metadata = metadata;

	}

	public TraceJsonFile(String description, List<PersistentTransition> trace, List<String> variableNames,
						 Map<String, OperationInfo> machineOperationInfos, List<String> constantNames, List<String> setNames,
						 JsonMetadata metadata) {


		this.transitionList = trace;
		this.description = description;
		this.variableNames = variableNames;
		this.constantNames = constantNames;
		this.setNames = setNames;
		this.machineOperationInfos = machineOperationInfos;
		this.globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		this.reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
		this.metadata = metadata;

	}

	public static JsonMetadataBuilder metadataBuilder() {
		return new JsonMetadataBuilder(FILE_TYPE, CURRENT_FORMAT_VERSION)
			.withUserCreator()
			.withSavedNow();
	}

	/**
	 * Gets the global Type Infos, and the OperationInfos and produces new OperationInfos with the global type infos
	 * worked into
	 * @param globalTypeInfos the global infos
	 * @param operationInfoMap the stored OperationInfos
	 * @return the proper OperationInfos
	 */
	public static Map<String, OperationInfo> reassembleTypeInfo(Map<String, String> globalTypeInfos, Map<String, OperationInfo> operationInfoMap){
		return  operationInfoMap.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> {

					List<String> identifiers = entry.getValue().getAllVariables();
					Map<String, String> bla = globalTypeInfos.entrySet().stream().filter(innerEntry -> identifiers.contains(innerEntry.getKey()))
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
					HashMap<String, String> oldMap = new HashMap<>(entry.getValue().getTypeMap());
					oldMap.putAll(bla);

					return entry.getValue().createOperationInfoWithNewTypeMap(oldMap);
				}));
	}

	/**
	 * Gets a map of OperationInfos and extract the type infos that are used in every operation
	 * @param infos the map of operationInfos
	 * @return the map of global type infos
	 */
	public static Map<String, String> createGlobalIdentifierMap(Map<String, OperationInfo> infos){
		return infos.values().stream()
				.flatMap(entry -> entry.getTypeMap().entrySet().stream().filter(innerEntry -> entry.getAllVariables().contains(innerEntry.getKey())))
				.collect(Collectors.toSet())
				.stream()
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	/**
	 * Removes global identifier typing from operation infos
	 * @param infos the infos to remove the typing from
	 * @param globalIdentifierTypes the identifiers to be removed
	 * @return the cleansed infos
	 */
	public static Map<String, OperationInfo> cleanseOperationInfo(Map<String, OperationInfo> infos, Map<String, String> globalIdentifierTypes) {
		return infos.entrySet().stream().collect(toMap(Map.Entry::getKey, entry ->
				entry.getValue().createOperationInfoWithNewTypeMap(entry.getValue().getTypeMap()
						.entrySet()
						.stream()
						.filter(innerEntry -> !globalIdentifierTypes.containsKey(innerEntry.getKey()))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue)))));
	}



	public List<PersistentTransition> getTransitionList() {
		return transitionList;
	}

	public List<String> getVariableNames() {
		return variableNames;
	}

	@JsonIgnore
	public Map<String, OperationInfo> getMachineOperationInfos() {
		return machineOperationInfos;
	}

	public List<String> getConstantNames() {
		return constantNames;
	}

	public List<String> getSetNames() {
		return setNames;
	}

	@JsonProperty("machineOperationInfos")
	public Map<String, OperationInfo> getReducedMachineOperationInfos(){return reducedMachineOperationInfos;}

	public Map<String, String> getGlobalIdentifierTypes(){return globalIdentifierTypes;}

	public String getDescription(){
		return description;
	}

	@Override
	public JsonMetadata getMetadata(){
		return metadata;
	}

	@Override
	public TraceJsonFile withMetadata(final JsonMetadata metadata) {
		return new TraceJsonFile(description, transitionList, variableNames, machineOperationInfos, constantNames, setNames, metadata);
	}

	public TraceJsonFile changeTrace(PersistentTrace trace){
		return new TraceJsonFile(trace, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeDescription(String description){
		return new TraceJsonFile(description, transitionList, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeTransitionList(List<PersistentTransition> trace){
		return new TraceJsonFile(description, trace, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeModelName(String name){
		return this.withMetadata(getMetadata().changeModelName(name));
	}

	public TraceJsonFile changeMachineInfos(Map<String, OperationInfo> operationInfoMap){
		return new TraceJsonFile(description, transitionList, variableNames, operationInfoMap, constantNames, setNames, getMetadata());
	}

}
