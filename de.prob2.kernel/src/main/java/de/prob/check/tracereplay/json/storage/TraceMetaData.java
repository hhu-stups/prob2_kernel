package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A container for the meta data of a json file
 * this one is empty and servers as token for trace meta data
 */
public class TraceMetaData extends AbstractMetaData {

	/**
	 *  @param formatVersion the format version
	 * @param savedAt saved when
	 * @param creator created by whom
	 * @param proBCliVersion probcli version
	 * @param name name
	 */
	public TraceMetaData(@JsonProperty("formatVersion") int formatVersion,
						 @JsonProperty("savedAt") LocalDateTime savedAt,
						 @JsonProperty("creator") String creator,
						 @JsonProperty("proBCliVersion") String proBCliVersion,
						 @JsonProperty("name") String name) {
		super(formatVersion, savedAt, creator, proBCliVersion, name);
	}
}
