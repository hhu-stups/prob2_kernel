package de.prob.check.tracereplay.json.storage;

import java.time.LocalDate;

/**
 * A container for the meta data of a json file
 * this one is empty and servers as token for trace meta data
 */
public class TraceMetaData extends AbstractMetaData {
	public TraceMetaData(int formatVersion, LocalDate savedAt, String creator, String proBCliVersion, String name) {
		super(formatVersion, savedAt, creator, proBCliVersion, name);
	}
}
