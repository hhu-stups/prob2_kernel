package de.prob.check.tracereplay;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import de.prob.json.JsonManager;
import de.prob.json.JsonMetadata;
import de.prob.json.ObjectWithMetadata;

public class TraceLoaderSaver {

	private final JsonManager<PersistentTrace> jsonManager;

	@Inject
	public TraceLoaderSaver(JsonManager<PersistentTrace> jsonManager) {
		this.jsonManager = jsonManager;
		final Gson gson = new GsonBuilder()
			.disableHtmlEscaping()
			.serializeNulls()
			.setPrettyPrinting()
			.create();
		jsonManager.initContext(new JsonManager.Context<PersistentTrace>(gson, PersistentTrace.class, "Trace", 1) {
			@Override
			public ObjectWithMetadata<JsonObject> convertOldData(final JsonObject oldObject, final JsonMetadata oldMetadata) {
				if (oldMetadata.getFileType() == null) {
					assert oldMetadata.getFormatVersion() == 0;
					if (!oldObject.has("transitionList")) {
						throw new JsonParseException("Not a valid trace file - missing required field transitionList");
					}
				}
				return new ObjectWithMetadata<>(oldObject, oldMetadata);
			}
		});
	}

	public PersistentTrace load(Path path) throws IOException {
		return this.jsonManager.readFromFile(path).getObject();
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
