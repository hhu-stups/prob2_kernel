package de.prob.check.tracereplay.json.storage;

import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.statespace.LoadedMachine;
import de.prob.statespace.OperationInfo;

import java.util.List;
import java.util.Map;

/**
 * Represents the trace file
 */
public class TraceJsonFile extends AbstractJsonFile{

	private final PersistentTrace trace;
	private final List<String> variableNames;
	private final Map<String, OperationInfo> machineOperationInfos;
	private final List<String> constantNames;
	private final List<String> setNames;

	/**
	 *
	 * @param name name of the file
	 * @param description description of the file
	 * @param trace the trace to be stored
	 * @param machine the machine corresponding to the trace when it was created
	 * @param metaData the meta data
	 */
	public TraceJsonFile(String name, String description, PersistentTrace trace, LoadedMachine machine, AbstractMetaData metaData) {
		super(name, description, metaData);
		this.trace = trace;
		variableNames = machine.getVariableNames();
		constantNames = machine.getConstantNames();
		setNames = machine.getSetNames();
		machineOperationInfos = machine.getOperations();

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


}
