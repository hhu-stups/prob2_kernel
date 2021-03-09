package de.prob.check.tracereplay;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.json.JsonMetadata;

@Deprecated
public class TraceLoaderSaver {
	private final TraceManager traceManager;

	@Deprecated
	@Inject
	public TraceLoaderSaver(final TraceManager traceManager) {
		this.traceManager = traceManager;
	}

	@Deprecated
	public PersistentTrace load(Path path) throws IOException {
		final TraceJsonFile traceJsonFile;
		try {
			traceJsonFile = this.traceManager.load(path);
		} catch (JsonProcessingException e) {
			throw new JsonParseException(e);
		}
		return new PersistentTrace(traceJsonFile.getDescription(), traceJsonFile.getTransitionList());
	}

	private void save(final Path location, final PersistentTrace trace, final JsonMetadata metadata) throws IOException {
		final TraceJsonFile traceJsonFile = new TraceJsonFile(
			trace.getDescription(),
			trace.getTransitionList(),
			Collections.emptyList(),
			Collections.emptyMap(),
			Collections.emptyList(),
			Collections.emptyList(),
			metadata
		);
		try {
			this.traceManager.save(location, traceJsonFile);
		} catch (JsonProcessingException e) {
			throw new JsonParseException(e);
		}
	}

	@Deprecated
	public void save(PersistentTrace trace, Path location, String proBCliVersion, String modelName) throws IOException {
		final JsonMetadata metadata = TraceJsonFile.metadataBuilder()
			.withProBCliVersion(proBCliVersion)
			.withModelName(modelName)
			.build();
		this.save(location, trace, metadata);
	}

	@Deprecated
	public void save(PersistentTrace trace, Path location) throws IOException {
		this.save(location, trace, TraceJsonFile.metadataBuilder().build());
	}
}
