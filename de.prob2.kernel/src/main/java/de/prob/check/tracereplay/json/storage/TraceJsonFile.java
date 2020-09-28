package de.prob.check.tracereplay.json.storage;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.statespace.LoadedMachine;

/**
 * Represents the trace file
 */
public class TraceJsonFile extends AbstractJsonFile{

	private PersistentTrace trace;
	private LoadedMachine machine;

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
		this.machine = machine;
	}


	public PersistentTrace getTrace() {
		return trace;
	}

	public void setTrace(PersistentTrace trace) {
		this.trace = trace;
	}

	public LoadedMachine getMachine() {
		return machine;
	}

	public void setMachine(LoadedMachine machine) {
		this.machine = machine;
	}

}
