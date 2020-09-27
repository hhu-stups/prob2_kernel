package de.prob.check.tracereplay.json;

import com.google.inject.Inject;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.storage.JsonFile;

import de.prob.check.tracereplay.json.storage.TraceMetaData;
import de.prob.check.tracereplay.json.storage.TraceStorage;
import de.prob.statespace.StateSpace;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Loads and safes traces
 */
public class TraceManager {

	private final JsonManager jsonManager;

	@Inject
	public TraceManager(JsonManager jsonManager){
		this.jsonManager = jsonManager;
	}


	/**
	 * Loads a trace
	 * @param path were to look
	 * @return a loaded trace
	 * @throws IOException something went wrong while loading
	 */
	public JsonFile<TraceStorage> load(Path path) throws IOException {
		return jsonManager.load(path);
	}


	/**
	 * saves a trace
	 * @param trace the trace to save
	 * @param stateSpace the corresponding state space
	 * @param location where to save
	 * @param proBCliVersion the probcli version
	 * @param modelName the name of the machine the trace was created from
	 * @throws IOException something went wrong with saving
	 */
	public void save(PersistentTrace trace, StateSpace stateSpace, Path location, String proBCliVersion, String modelName) throws IOException {
		TraceStorage traceStorage = new TraceStorage(trace, stateSpace.getLoadedMachine());
		TraceMetaData traceMetaData = new TraceMetaData(1, LocalDate.now(), "User", proBCliVersion, modelName);
		jsonManager.save(location, traceStorage, traceMetaData);
	}

}
