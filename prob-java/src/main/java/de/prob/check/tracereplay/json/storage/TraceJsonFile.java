package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Represents the trace file
 */
@JsonPropertyOrder({ "description", "transitionList", "variableNames", "constantNames", "setNames", "machineOperationInfos", "globalIdentifierTypes", "metadata" })
public class TraceJsonFile implements HasMetadata {

	public static final String FILE_TYPE = "Trace";
	public static final int CURRENT_FORMAT_VERSION = 6;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final String description;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<PersistentTransition> transitionList;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<String> variableNames;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<String> constantNames;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<String> setNames;
	/**
	 * Generated from {@link TraceJsonFile#reducedMachineOperationInfos} and {@link TraceJsonFile#globalIdentifierTypes} at JSON load time.
	 */
	private final Map<String, OperationInfo> machineOperationInfos;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final Map<String, OperationInfo> reducedMachineOperationInfos;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final Map<String, String> globalIdentifierTypes;
	private final JsonMetadata metadata;

	/**
	 * @param trace    the trace to be stored
	 * @param metadata the metadata
	 */
	public TraceJsonFile(Trace trace, JsonMetadata metadata) {
		this(new PersistentTrace(trace), trace.getStateSpace().getLoadedMachine(), metadata);
	}

	/**
	 * @param trace    the trace to be stored
	 * @param machine  the machine corresponding to the trace when it was created
	 * @param metadata the metadata
	 */
	public TraceJsonFile(PersistentTrace trace, LoadedMachine machine, JsonMetadata metadata) {
		this(trace.getDescription(), trace.getTransitionList(), machine.getVariableNames(), machine.getOperations(), machine.getConstantNames(), machine.getSetNames(), metadata);
	}

	public TraceJsonFile(
		PersistentTrace trace,
		List<String> variableNames,
		Map<String, OperationInfo> machineOperationInfos,
		List<String> constantNames,
		List<String> setNames,
		JsonMetadata metadata
	) {
		this(trace.getDescription(), trace.getTransitionList(), variableNames, machineOperationInfos, constantNames, setNames, metadata);
	}

	public TraceJsonFile(
		String description,
		List<PersistentTransition> transitionList,
		List<String> variableNames,
		Map<String, OperationInfo> machineOperationInfos,
		List<String> constantNames,
		List<String> setNames,
		JsonMetadata metadata
	) {
		this.transitionList = transitionList != null ? transitionList : new ArrayList<>();
		this.description = description != null ? description : "";
		this.variableNames = variableNames != null ? variableNames : new ArrayList<>();
		this.constantNames = constantNames != null ? constantNames : new ArrayList<>();
		this.setNames = setNames != null ? setNames : new ArrayList<>();
		this.machineOperationInfos = machineOperationInfos != null ? machineOperationInfos : new HashMap<>();
		this.globalIdentifierTypes = createGlobalIdentifierMap(this.machineOperationInfos);
		this.reducedMachineOperationInfos = cleanseOperationInfo(this.machineOperationInfos, globalIdentifierTypes);
		this.metadata = metadata;
	}

	/**
	 * The constructor to deserialize files
	 *
	 * @param description           description
	 * @param transitionList        trace
	 * @param variableNames         variable names of the corresponding machine
	 * @param machineOperationInfos machine operation infos of the corresponding machine
	 * @param constantNames         name of constants infos of the corresponding machine
	 * @param setNames              name of sets operation infos of the corresponding machine
	 * @param metadata              metadata
	 */
	public TraceJsonFile(
		@JsonProperty("description") String description,
		@JsonProperty("transitionList") List<PersistentTransition> transitionList,
		@JsonProperty("variablesNames") List<String> variableNames,
		@JsonProperty("machineOperationInfos") Map<String, OperationInfo> machineOperationInfos,
		@JsonProperty("constantNames") List<String> constantNames,
		@JsonProperty("setNames") List<String> setNames,
		@JsonProperty("globalIdentifierTypes") Map<String, String> globalIdentifierTypes,
		@JsonProperty("metadata") JsonMetadata metadata
	) {
		this.transitionList = transitionList != null ? transitionList : new ArrayList<>();
		this.description = description != null ? description : "";
		this.variableNames = variableNames != null ? variableNames : new ArrayList<>();
		this.constantNames = constantNames != null ? constantNames : new ArrayList<>();
		this.setNames = setNames != null ? setNames : new ArrayList<>();
		this.reducedMachineOperationInfos = machineOperationInfos != null ? machineOperationInfos : new HashMap<>();
		this.globalIdentifierTypes = globalIdentifierTypes != null ? globalIdentifierTypes : new HashMap<>();
		this.machineOperationInfos = reassembleTypeInfo(this.globalIdentifierTypes, this.reducedMachineOperationInfos);
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
	 *
	 * @param globalTypeInfos  the global infos
	 * @param operationInfoMap the stored OperationInfos
	 * @return the proper OperationInfos
	 */
	public static Map<String, OperationInfo> reassembleTypeInfo(Map<String, String> globalTypeInfos, Map<String, OperationInfo> operationInfoMap) {
		return operationInfoMap.entrySet()
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
	 *
	 * @param infos the map of operationInfos
	 * @return the map of global type infos
	 */
	public static Map<String, String> createGlobalIdentifierMap(Map<String, OperationInfo> infos) {
		return infos.values().stream()
			.flatMap(entry -> entry.getTypeMap().entrySet().stream().filter(innerEntry -> entry.getAllVariables().contains(innerEntry.getKey())))
			.collect(Collectors.toSet())
			.stream()
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Removes global identifier typing from operation infos
	 *
	 * @param infos                 the infos to remove the typing from
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
	public Map<String, OperationInfo> getReducedMachineOperationInfos() {return reducedMachineOperationInfos;}

	public Map<String, String> getGlobalIdentifierTypes() {return globalIdentifierTypes;}

	public String getDescription() {
		return description;
	}

	@Override
	public JsonMetadata getMetadata() {
		return metadata;
	}

	@Override
	public TraceJsonFile withMetadata(final JsonMetadata metadata) {
		return new TraceJsonFile(description, transitionList, variableNames, machineOperationInfos, constantNames, setNames, metadata);
	}

	public TraceJsonFile updateMetaData() {
		return new TraceJsonFile(description, transitionList, variableNames, machineOperationInfos, constantNames, setNames, metadataBuilder().build());
	}

	public TraceJsonFile changeTrace(PersistentTrace trace) {
		return new TraceJsonFile(trace, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeTrace(List<PersistentTransition> trace) {
		return new TraceJsonFile(description, trace, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeDescription(String description) {
		return new TraceJsonFile(description, transitionList, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeTransitionList(List<PersistentTransition> trace) {
		return new TraceJsonFile(description, trace, variableNames, machineOperationInfos, constantNames, setNames, getMetadata());
	}

	public TraceJsonFile changeModelName(String name) {
		return this.withMetadata(getMetadata().changeModelName(name));
	}

	public TraceJsonFile changeMachineInfos(Map<String, OperationInfo> operationInfoMap) {
		return new TraceJsonFile(description, transitionList, variableNames, operationInfoMap, constantNames, setNames, getMetadata());
	}
}
