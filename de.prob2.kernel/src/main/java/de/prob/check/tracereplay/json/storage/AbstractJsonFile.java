package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.prob.json.JsonMetadata;

/**
 * Represents a abstract json file
 * More detailed information about the payload are defined in subclasses
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "type"
)
@JsonSubTypes(
		@JsonSubTypes.Type(value = TraceJsonFile.class, name = "Trace")
)
public abstract class AbstractJsonFile {


	private final String name;
	private final String description;
	private final JsonMetadata metadata;


	/**
	 *
	 * @param name the name of the json file
	 * @param description a description of the json file
	 * @param metadata the metadata
	 */
	public AbstractJsonFile(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("metadata") JsonMetadata metadata) {
		this.name = name;
		this.description = description;
		this.metadata = metadata;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public JsonMetadata getMetadata() {
		return metadata;
	}


}
