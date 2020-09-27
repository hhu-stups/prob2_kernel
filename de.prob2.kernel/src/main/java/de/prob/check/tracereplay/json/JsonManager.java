package de.prob.check.tracereplay.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import de.prob.check.tracereplay.json.storage.AbstractMetaData;
import de.prob.check.tracereplay.json.storage.JsonFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Writes Objects to Json
 * Reads Files and Converts them into objects
 * For the file structure 
 * 
 */
public class JsonManager {


	private final ObjectMapper objectMapper;

	@Inject
	public JsonManager(ObjectMapper objectMapper){
		this.objectMapper = objectMapper;
		objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	/**
	 *
	 * @param path the path to load from
	 * @param <T> the expected data structure
	 * @return an object of the form <T>
	 * @throws IOException parse exception
	 */
	public <T> JsonFile<T> load(Path path) throws IOException {
		return objectMapper.readValue(path.toFile(), new TypeReference<JsonFile<T>>(){});
	}

	/**
	 *
	 * @param location where to save
	 * @param object the object to be stored
	 * @param <T> the type of the object to be stored
	 * @throws IOException couldn't write object
	 */
	public<T> void save(Path location, T object, AbstractMetaData metaData) throws IOException {
		JsonFile<T> jsonFile = new JsonFile<>("", "", object, metaData);
		objectMapper.writeValue(location.toFile(), jsonFile);
	}


}
