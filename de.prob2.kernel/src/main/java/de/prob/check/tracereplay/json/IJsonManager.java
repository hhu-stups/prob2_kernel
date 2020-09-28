package de.prob.check.tracereplay.json;

import de.prob.check.tracereplay.json.storage.AbstractJsonFile;

import java.io.IOException;
import java.nio.file.Path;


public interface IJsonManager{
	
	/**
	 *
	 * @param path the path to load from
	 * @return an object of the form AbstractJsonFile
	 */
	AbstractJsonFile load(Path path) throws IOException;

	/**
	 *
	 * @param location where to save
	 * @param object the object to be stored
	 */
	void save(Path location, AbstractJsonFile object) throws IOException;

}
