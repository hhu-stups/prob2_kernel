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
	private final DefaultTraceReplayFileHandler defaultFileHandler;

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
		this.defaultFileHandler = new DefaultTraceReplayFileHandler();
	}

	public PersistentTrace load(Path path, ITraceReplayFileHandler fileHandler) {
		try {
			return this.jsonManager.readFromFile(path).getObject();
		} catch (JsonParseException | IOException e) {
			fileHandler.showLoadError(path, e);
			return null;
		}
	}

	public PersistentTrace load(Path path) {
		return this.load(path, new DefaultTraceReplayFileHandler());
	}

	public void save(PersistentTrace trace, Path location, ITraceReplayFileHandler fileHandler, String proB2KernelVersion, String proBCliVersion, String modelName) {
		try {
			final JsonMetadata metadata = this.jsonManager.defaultMetadataBuilder()
				.withProB2KernelVersion(proB2KernelVersion)
				.withProBCliVersion(proBCliVersion)
				.withModelName(modelName)
				.build();
			this.jsonManager.writeToFile(location, trace, metadata);
		} catch (IOException e) {
			fileHandler.showSaveError(e);
		}
	}

	public void save(PersistentTrace trace, Path location, String proB2KernelVersion, String proBCliVersion, String modelName) {
		this.save(trace, location, defaultFileHandler, proB2KernelVersion, proBCliVersion, modelName);
	}

	public void save(PersistentTrace trace, Path location, ITraceReplayFileHandler fileHandler) {
		try {
			this.jsonManager.writeToFile(location, trace);
		} catch (IOException e) {
			fileHandler.showSaveError(e);
		}
	}

	public void save(PersistentTrace trace, Path location) {
		this.save(trace, location, defaultFileHandler);
	}

	public JsonManager<PersistentTrace> getJsonManager() {
		return jsonManager;
	}
}
