package de.prob.check.tracereplay.json.storage;

import java.time.LocalDate;

/**
 * A container for the meta data of a json file
 * this one is empty and servers as token for trace meta data
 */
public class TraceMetaData extends AbstractMetaData {

	/**
	 *
	 * @param formatVersion the format version
	 * @param savedAt saved when
	 * @param creator created by whom
	 * @param proBCliVersion probcli version
	 * @param name name
	 */
	public TraceMetaData(int formatVersion, LocalDate savedAt, String creator, String proBCliVersion, String name) {
		super(formatVersion, savedAt, creator, proBCliVersion, name);
	}
}
