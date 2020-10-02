package de.prob.check.tracereplay.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.storage.AbstractJsonFile;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.json.JsonManager;
import de.prob.json.JsonMetadata;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads and safes traces
 */
public class TraceManager implements IJsonManager {

	private final ObjectMapper objectMapper;

	@Inject
	public TraceManager(ObjectMapper objectMapper){

		this.objectMapper = objectMapper;
		this.objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		this.objectMapper.enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		this.objectMapper.registerModule(new JavaTimeModule());
	}

	/**
	 * @param path the path to load from
	 * @return an object of the form AbstractJsonFile
	 */
	@Override
	public TraceJsonFile load(Path path) throws IOException {
		return objectMapper.readValue(path.toFile(), TraceJsonFile.class);
	}


	/**
	 * @param location where to save
	 * @param object   the object to be stored
	 */
	@Override
	public void save(Path location, AbstractJsonFile object) throws IOException {
		this.objectMapper.writeValue(location.toFile(), object);
	}

}
