package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
	private final AbstractMetaData metaData;


	/**
	 *
	 * @param name the name of the json file
	 * @param description a description of the json file
	 * @param metaData the meta data
	 */
	public AbstractJsonFile(String name, String description, AbstractMetaData metaData) {
		this.name = name;
		this.description = description;
		this.metaData = metaData;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public AbstractMetaData getMetaData() {
		return metaData;
	}


}
