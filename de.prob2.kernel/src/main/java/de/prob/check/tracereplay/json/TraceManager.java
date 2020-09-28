package de.prob.check.tracereplay.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.storage.AbstractJsonFile;

import de.prob.check.tracereplay.json.storage.TraceMetaData;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.statespace.StateSpace;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Loads and safes traces
 */
public class TraceManager implements IJsonManager{

	private final ObjectMapper objectMapper;
	
	@Inject
	public TraceManager(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	

	/**
	 * Loads a trace
	 * @param path were to look
	 * @return a loaded trace
	 * @throws IOException something went wrong while loading
	 */
	@Override
	public TraceJsonFile load(Path path) throws IOException {
		return objectMapper.readValue(path.toFile(), TraceJsonFile.class);
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
		TraceMetaData traceMetaData = new TraceMetaData(1, LocalDate.now(), "User", proBCliVersion, modelName);
		TraceJsonFile abstractJsonFile = new TraceJsonFile("", "", trace, stateSpace.getLoadedMachine(), traceMetaData);
		save(location, abstractJsonFile);
	}


	/**
	 * @param location where to save
	 * @param object   the object to be stored
	 * @throws IOException something went wrong while writing
	 */
	@Override
	public void save(Path location, AbstractJsonFile object) throws IOException {
		objectMapper.writeValue(location.toFile(), object);
	}


}
