package de.prob.check.tracereplay.json;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.json.JacksonManager;
import de.prob.json.JsonConversionException;

/**
 * Loads and saves traces
 */
public class TraceManager {

	private static final int MAX_STRING_LENGTH = 0x4000000;

	private final JacksonManager<TraceJsonFile> jacksonManager;

	@Inject
	public TraceManager(ObjectMapper objectMapper, final JacksonManager<TraceJsonFile> jacksonManager) {
		ObjectMapper modifiedObjectMapper = objectMapper;
		// Increase the maximum string length to allow loading trace files with very large formulas.
		if (modifiedObjectMapper.getFactory().streamReadConstraints().getMaxStringLength() < MAX_STRING_LENGTH) {
			modifiedObjectMapper = modifiedObjectMapper.copyWith(new JsonFactoryBuilder()
				.streamReadConstraints(StreamReadConstraints.builder()
					.maxStringLength(MAX_STRING_LENGTH) // around 67 million chars, 64 MiB in ASCII, 128 MiB in UTF-16
					.build())
				.build());
		}

		this.jacksonManager = jacksonManager;
		this.jacksonManager.initContext(new JacksonManager.Context<TraceJsonFile>(modifiedObjectMapper, TraceJsonFile.class, TraceJsonFile.FILE_TYPE, TraceJsonFile.CURRENT_FORMAT_VERSION) {
			@Override
			public boolean shouldAcceptOldMetadata() {
				return true;
			}

			@Override
			public ObjectNode convertOldData(final ObjectNode oldObject, final int oldVersion) {
				if (!oldObject.has("transitionList")) {
					oldObject.set("transitionList", oldObject.arrayNode());
				}

				if (oldVersion <= 0) {
					if (!oldObject.has("description")) {
						oldObject.put("description", "");
					}
				}

				if (oldVersion <= 1) {
					for (final String listFieldName : new String[] { "variableNames", "constantNames", "setNames" }) {
						if (!oldObject.has(listFieldName)) {
							oldObject.set(listFieldName, oldObject.arrayNode());
						}
					}
					for (final String objectFieldName : new String[] { "machineOperationInfos", "globalIdentifierTypes" }) {
						if (!oldObject.has(objectFieldName)) {
							oldObject.set(objectFieldName, oldObject.objectNode());
						}
					}
					for (final JsonNode transitionNode : oldObject.get("transitionList")) {
						final ObjectNode transition = (ObjectNode) transitionNode;
						for (final String objectFieldName : new String[] { "params", "results", "destState" }) {
							if (!transition.has(objectFieldName) || transition.get(objectFieldName).isNull()) {
								transition.set(objectFieldName, transition.objectNode());
							}
						}
						for (final String arrayFieldName : new String[] { "destStateNotChanged", "preds" }) {
							if (!transition.has(arrayFieldName) || transition.get(arrayFieldName).isNull()) {
								transition.set(arrayFieldName, transition.arrayNode());
							}
						}
					}
				}

				if (oldVersion <= 2) {
					for (final JsonNode transitionNode : oldObject.get("transitionList")) {
						final ObjectNode transition = (ObjectNode) transitionNode;
						transition.set("postconditions", transition.arrayNode());
					}
				}

				if (oldVersion <= 3) {
					for (final JsonNode transitionNode : oldObject.get("transitionList")) {
						final ObjectNode transition = (ObjectNode) transitionNode;
						if (!transition.has("description")) {
							transition.put("description", "");
						}
					}
				}

				if (oldVersion <= 4) {
					for (final JsonNode transitionNode : oldObject.get("transitionList")) {
						final ObjectNode transition = (ObjectNode) transitionNode;
						for (final JsonNode postconditionNode : transition.get("postconditions")) {
							final ObjectNode postcondition = (ObjectNode) postconditionNode;
							String kind = postcondition.get("kind").asText();
							if ("PREDICATE".equals(kind)) {
								postcondition.put("predicate", postcondition.get("value").asText());
							} else if ("ENABLEDNESS".equals(kind)) {
								postcondition.put("operation", postcondition.get("value").asText());
								postcondition.put("predicate", "");
							}
							postcondition.remove("value");
						}
					}
				}

				// version 6 makes a lot of the properties optional, no rewrites required

				return oldObject;
			}
		});
	}

	/**
	 * @param path the path to load from
	 * @return an object of the form AbstractJsonFile
	 */
	public TraceJsonFile load(Path path) throws IOException {
		return this.jacksonManager.readFromFile(path);
	}

	/**
	 * @param location where to save
	 * @param object   the object to be stored
	 */
	public void save(Path location, TraceJsonFile object) throws IOException {
		this.jacksonManager.writeToFile(location, object);
	}
}
