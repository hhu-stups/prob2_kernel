package de.prob.check.tracereplay.json;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.json.JacksonManager;
import de.prob.json.JsonConversionException;

/**
 * Loads and safes traces
 */
public class TraceManager  {
	private final JacksonManager<TraceJsonFile> jacksonManager;

	@Inject
	public TraceManager(ObjectMapper objectMapper, final JacksonManager<TraceJsonFile> jacksonManager) {
		this.jacksonManager = jacksonManager;
		this.jacksonManager.initContext(new JacksonManager.Context<TraceJsonFile>(objectMapper, TraceJsonFile.class, TraceJsonFile.FILE_TYPE, TraceJsonFile.CURRENT_FORMAT_VERSION, true) {
			@Override
			public ObjectNode convertOldData(final ObjectNode oldObject, final int oldVersion) {
				if (oldVersion <= 0) {
					if (!oldObject.has("transitionList")) {
						throw new JsonConversionException("Not a valid trace file - missing required field transitionList");
					}
					if (!oldObject.has("description")) {
						oldObject.put("description", "");
					}
				}

				if (oldVersion <= 1) {
					for (final String listFieldName : new String[] {"variableNames", "constantNames", "setNames"}) {
						if (!oldObject.has(listFieldName)) {
							oldObject.set(listFieldName, oldObject.arrayNode());
						}
					}
					for (final String objectFieldName : new String[] {"machineOperationInfos", "globalIdentifierTypes"}) {
						if (!oldObject.has(objectFieldName)) {
							oldObject.set(objectFieldName, oldObject.objectNode());
						}
					}
					for (final JsonNode transitionNode : oldObject.get("transitionList")) {
						final ObjectNode transition = (ObjectNode)transitionNode;
						for (final String objectFieldName : new String[] {"params", "results", "destState"}) {
							if (!transition.has(objectFieldName) || transition.get(objectFieldName).isNull()) {
								transition.set(objectFieldName, transition.objectNode());
							}
						}
						for (final String arrayFieldName : new String[] {"destStateNotChanged", "preds"}) {
							if (!transition.has(arrayFieldName) || transition.get(arrayFieldName).isNull()) {
								transition.set(arrayFieldName, transition.arrayNode());
							}
						}
					}
				}

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
