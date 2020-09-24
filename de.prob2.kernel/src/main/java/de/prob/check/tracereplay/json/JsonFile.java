package de.prob.check.tracereplay.json;

/**
 * Represents a json file
 * payload is any object
 */
public class JsonFile<T> {
	private String name;
	private String description;
	private T payload;
	private AbstractMetaData metaData;


}
