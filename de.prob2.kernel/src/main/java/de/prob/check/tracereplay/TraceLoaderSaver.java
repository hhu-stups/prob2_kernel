package de.prob.check.tracereplay;

import java.io.IOException;
import java.nio.file.Path;

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
		jsonManager.initContext(new JsonManager.Context<PersistentTrace>(PersistentTrace.class, "Trace", 1) {
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

	/**
	 * @deprecated Use {@link #load(Path)} instead. Errors are reported as exceptions.
	 */
	@Deprecated
	public PersistentTrace load(Path path, ITraceReplayFileHandler fileHandler) {
		try {
			return this.jsonManager.readFromFile(path).getObject();
		} catch (JsonParseException | IOException e) {
			fileHandler.showLoadError(path, e);
			return null;
		}
	}

	public PersistentTrace load(Path path) throws IOException {
		return this.jsonManager.readFromFile(path).getObject();
	}

	/**
	 * @deprecated Use {@link #save(PersistentTrace, Path, String, String)} instead. Errors are reported as exceptions.
	 */
	@Deprecated
	public void save(PersistentTrace trace, Path location, ITraceReplayFileHandler fileHandler, String proBCliVersion, String modelName) {
		try {
			this.save(trace, location, proBCliVersion, modelName);
		} catch (IOException e) {
			fileHandler.showSaveError(e);
		}
	}

	public void save(PersistentTrace trace, Path location, String proBCliVersion, String modelName) throws IOException {
		final JsonMetadata metadata = this.jsonManager.defaultMetadataBuilder()
			.withProBCliVersion(proBCliVersion)
			.withModelName(modelName)
			.build();
		this.jsonManager.writeToFile(location, trace, metadata);
	}

	/**
	 * @deprecated Use {@link #save(PersistentTrace, Path)} instead. Errors are reported as exceptions.
	 */
	@Deprecated
	public void save(PersistentTrace trace, Path location, ITraceReplayFileHandler fileHandler) {
		try {
			this.save(trace, location);
		} catch (IOException e) {
			fileHandler.showSaveError(e);
		}
	}

	public void save(PersistentTrace trace, Path location) throws IOException {
		this.jsonManager.writeToFile(location, trace);
	}

	public JsonManager<PersistentTrace> getJsonManager() {
		return jsonManager;
	}
}
