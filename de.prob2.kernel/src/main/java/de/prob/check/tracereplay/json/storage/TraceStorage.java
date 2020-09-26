package de.prob.check.tracereplay.json.storage;

import de.prob.check.tracereplay.PersistentTrace;
import de.prob.statespace.LoadedMachine;

public class TraceStorage {

	private PersistentTrace trace;
	private LoadedMachine machine;


	public TraceStorage(PersistentTrace trace, LoadedMachine machine) {
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
