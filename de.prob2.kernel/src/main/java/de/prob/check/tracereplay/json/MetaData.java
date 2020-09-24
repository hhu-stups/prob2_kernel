package de.prob.check.tracereplay.json;

/**
 * A container for the meta data of a json file
 */
public class MetaData extends AbstractMetaData {

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	private Type type;

}
