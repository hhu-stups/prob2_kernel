package de.prob.json;

/**
 * An object that has {@link JsonMetadata} attached.
 * Must be implemented by all classes that are used with {@link JacksonManager}
 * as the class of the root object in a JSON file.
 */
public interface HasMetadata {
	public JsonMetadata getMetadata();
	
	/**
	 * Return a copy of this object with the metadata replaced.
	 * 
	 * @param metadata the new metadata
	 * @return a copy of this object with the metadata replaced
	 */
	public HasMetadata withMetadata(final JsonMetadata metadata);
}
