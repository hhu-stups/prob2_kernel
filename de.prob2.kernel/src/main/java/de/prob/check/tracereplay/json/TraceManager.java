package de.prob.check.tracereplay.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.storage.JsonFile;

import de.prob.check.tracereplay.json.storage.MetaData;
import de.prob.check.tracereplay.json.storage.TraceStorage;
import de.prob.statespace.StateSpace;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Loads and safes traces
 */
public class TraceManager {

	private final ObjectMapper objectMapper;

	@Inject
	public TraceManager(ObjectMapper objectMapper){
		this.objectMapper = objectMapper;
	}

	public <T> JsonFile<T> load(Path path) throws IOException {
		return objectMapper.readValue(path.toFile(), new TypeReference<JsonFile<T>>(){});
	}


	//TODO refactor to two methods
	public void save(PersistentTrace trace, StateSpace stateSpace, Path location, String proBCliVersion, String modelName) throws IOException {
		TraceStorage traceStorage = new TraceStorage(trace, stateSpace.getLoadedMachine());
		MetaData metaData = new MetaData(1, LocalDate.now(), "User", proBCliVersion, modelName);
		JsonFile<TraceStorage> jsonFile = new JsonFile<>("", "", traceStorage, metaData);
		objectMapper.writeValue(location.toFile(), jsonFile);
	}

}
