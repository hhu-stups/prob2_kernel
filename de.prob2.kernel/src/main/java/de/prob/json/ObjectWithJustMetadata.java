package de.prob.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Internal helper class used to extract just the metadata (in new format)
 * from a ProB 2 JSON file, without parsing the rest of the root object yet.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
final class ObjectWithJustMetadata {
	private final JsonMetadata metadata;
	
	private ObjectWithJustMetadata(@JsonProperty("metadata") final JsonMetadata metadata) {
		this.metadata = metadata;
	}
	
	JsonMetadata getMetadata() {
		return this.metadata;
	}
}
