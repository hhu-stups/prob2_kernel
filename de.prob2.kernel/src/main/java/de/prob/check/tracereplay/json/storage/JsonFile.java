package de.prob.check.tracereplay.json.storage;

import de.prob.check.tracereplay.json.storage.AbstractMetaData;

/**
 * Represents a json file
 * payload is any object
 */
public class JsonFile<T> {


	private String name;
	private String description;
	private T payload;
	private AbstractMetaData metaData;



	public JsonFile(String name, String description, T payload, AbstractMetaData metaData) {
		this.name = name;
		this.description = description;
		this.payload = payload;
		this.metaData = metaData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	public AbstractMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(AbstractMetaData metaData) {
		this.metaData = metaData;
	}



}
