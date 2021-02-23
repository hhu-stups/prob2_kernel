package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.json.JsonMetadata;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Trace;

import java.util.List;
import java.util.Map;

/**
 * Represents the trace file
 */
@JsonPropertyOrder({"name", "description", "path", "trace", "variableNames", "machineOperationInfos", "constantNames", "setNames", "metadata"})
public class TraceJsonFile extends AbstractJsonFile{

	private final PersistentTrace trace;
	private final List<String> variableNames;
	private final List<String> constantNames;
	private final List<String> setNames;
	private final Map<String, OperationInfo> machineOperationInfos;


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
						 @JsonProperty("metadata") JsonMetadata metadata) {

		super(name, description, metadata);
		this.trace = trace;
		this.variableNames = variableNames;
		this.constantNames = constantNames;
		this.setNames = setNames;
		this.machineOperationInfos = machineOperationInfos;
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
