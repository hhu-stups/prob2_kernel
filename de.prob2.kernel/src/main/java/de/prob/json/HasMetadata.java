package de.prob.json;

/**
 * An object that has {@link JsonMetadata} attached.
 * Must be implemented by all classes that are used with {@link JacksonManager}
 * as the class of the root object in a JSON file.
 */
public interface HasMetadata {
	JsonMetadata getMetadata();
	
	/**
	 * Return a copy of this object with the metadata replaced.
	 * 
	 * @param metadata the new metadata
	 * @return a copy of this object with the metadata replaced
	 */
    HasMetadata withMetadata(final JsonMetadata metadata);
}
