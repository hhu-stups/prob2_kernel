package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.json.JsonMetadata;
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
@JsonPropertyOrder({"name", "description", "path", "trace", "variableNames", "machineOperationInfos", "constantNames", "setNames", "globalIdentifierTypes", "metadata"})
public class TraceJsonFile extends AbstractJsonFile{

	private final PersistentTrace trace;
	private final List<String> variableNames;
	private final List<String> constantNames;
	private final List<String> setNames;
	@JsonIgnore  private final Map<String, OperationInfo> machineOperationInfos;
	private final Map<String, OperationInfo> reducedMachineOperationInfos;
	private final Map<String, String> globalIdentifierTypes;


	/**
	 *
	 * @param name name of the file
	 * @param description description of the file
	 * @param trace the trace to be stored
	 * @param metadata the metadata
	 */
	public TraceJsonFile(String name, String description, Trace trace, JsonMetadata metadata) {
		super(name, description, metadata);
		this.trace = new PersistentTrace(trace);
		variableNames = trace.getStateSpace().getLoadedMachine().getVariableNames();
		constantNames = trace.getStateSpace().getLoadedMachine().getConstantNames();
		setNames = trace.getStateSpace().getLoadedMachine().getSetNames();
		machineOperationInfos = trace.getStateSpace().getLoadedMachine().getOperations();
		globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
	}



	/**
	 *
	 * @param name name of the file
	 * @param description description of the file
	 * @param trace the trace to be stored
	 * @param machine the machine corresponding to the trace when it was created
	 * @param metadata the metadata
	 */
	public TraceJsonFile(String name, String description, PersistentTrace trace, LoadedMachine machine, JsonMetadata metadata) {
		super(name, description, metadata);
		this.trace = trace;
		variableNames = machine.getVariableNames();
		constantNames = machine.getConstantNames();
		setNames = machine.getSetNames();
		machineOperationInfos = machine.getOperations();
		globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
	}


	/**
	 * The constructor to deserialize files
	 * @param name the name
	 * @param description description
	 * @param trace trace
	 * @param variableNames variable names of the corresponding machine
	 * @param machineOperationInfos machine operation infos of the corresponding machine
	 * @param constantNames name of constants infos of the corresponding machine
	 * @param setNames name of sets operation infos of the corresponding machine
	 * @param metadata metadata
	 */
	public TraceJsonFile(@JsonProperty("name") String name,
						 @JsonProperty("description") String description,
						 @JsonProperty("trace") PersistentTrace trace,
						 @JsonProperty("variablesNames") List<String> variableNames,
						 @JsonProperty("machineOperationInfos") Map<String, OperationInfo> machineOperationInfos,
						 @JsonProperty("constantNames") List<String> constantNames,
						 @JsonProperty("setNames") List<String> setNames,
						 @JsonProperty("globalIdentifierTypes") Map<String, String> globalIdentifierTypes,
						 @JsonProperty("metadata") JsonMetadata metadata) {


		super(name, description, metadata);
		this.trace = trace;
		this.variableNames = variableNames;
		this.constantNames = constantNames;
		this.setNames = setNames;
		this.reducedMachineOperationInfos = machineOperationInfos;
		this.globalIdentifierTypes = globalIdentifierTypes;
		this.machineOperationInfos = reassembleTypeInfo(globalIdentifierTypes, machineOperationInfos);
	}

	private TraceJsonFile( String name, String description, PersistentTrace trace, List<String> variableNames,
						   Map<String, OperationInfo> machineOperationInfos, List<String> constantNames, List<String> setNames,
						   JsonMetadata metadata) {


		super(name, description, metadata);
		this.trace = trace;
		this.variableNames = variableNames;
		this.constantNames = constantNames;
		this.setNames = setNames;
		this.machineOperationInfos = machineOperationInfos;
		this.globalIdentifierTypes = createGlobalIdentifierMap(machineOperationInfos);
		this.reducedMachineOperationInfos = cleanseOperationInfo(machineOperationInfos, globalIdentifierTypes);
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
				.flatMap(entry -> entry.getTypeMap().entrySet().stream()).collect(Collectors.toSet())
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
		Map<String, OperationInfo> gna = infos.entrySet().stream().collect(toMap(Map.Entry::getKey, entry ->
				entry.getValue().createOperationInfoWithNewTypeMap(entry.getValue().getTypeMap()
						.entrySet()
						.stream()
						.filter(innerEntry -> !globalIdentifierTypes.containsKey(innerEntry.getKey()))
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue)))));

		return gna;
	}



	public PersistentTrace getTrace() {
		return trace;
	}


	public List<String> getVariableNames() {
		return variableNames;
	}

	public Map<String, OperationInfo> getMachineOperationInfos() {
		return machineOperationInfos;
	}

	public List<String> getConstantNames() {
		return constantNames;
	}

	public List<String> getSetNames() {
		return setNames;
	}


	public TraceJsonFile changeTrace(PersistentTrace trace){
		return new TraceJsonFile(super.getName(), getDescription(), trace, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

}
