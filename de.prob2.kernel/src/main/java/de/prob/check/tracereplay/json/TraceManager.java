package de.prob.check.tracereplay.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.json.JsonManager;
import de.prob.json.JsonMetadata;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads and safes traces
 */
public class TraceManager {

	private final ObjectMapper objectMapper;

	public TraceManager(ObjectMapper objectMapper){
		this.objectMapper = objectMapper;
	}

	public PersistentTrace load(Path path) throws IOException {
		return objectMapper.readValue(path.toFile(), PersistentTrace.class);
	}

	public void save(PersistentTrace trace, Path location, String proBCliVersion, String modelName) throws IOException {
		final JsonMetadata metadata = this.jsonManager.defaultMetadataBuilder()
				.withProBCliVersion(proBCliVersion)
				.withModelName(modelName)
				.build();
		this.jsonManager.writeToFile(location, trace, metadata);
	}

	public void save(PersistentTrace trace, Path location) throws IOException {
		this.jsonManager.writeToFile(location, trace);
	}

	public JsonManager<PersistentTrace> getJsonManager() {
		return jsonManager;
	}
}
