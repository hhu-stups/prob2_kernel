package de.prob.check.tracereplay.json.storage;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.prob.Main;

import java.time.LocalDateTime;


/**
 * Data container for meta data
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "type"
)
@JsonSubTypes(
		@JsonSubTypes.Type(value = TraceMetaData.class, name = "TraceMetaData")
)
public abstract class AbstractMetaData {

	private final int formatVersion;
	private final LocalDateTime savedAt;
	private final String creator;
	private final String proB2KernelVersion;
	private final String proBCliVersion;
	private final String name;

	/**
	 *
	 * @param formatVersion format version
	 * @param savedAt saved at time x
 	 * @param creator creator
	 * @param proBCliVersion probcli version
	 * @param name name
	 */
	public AbstractMetaData(int formatVersion, LocalDateTime savedAt, String creator, String proBCliVersion, String name) {
		this.formatVersion = formatVersion;
		this.savedAt = savedAt;
		this.creator = creator;
		this.proB2KernelVersion = Main.getVersion();
		this.proBCliVersion = proBCliVersion;
		this.name = name;
	}


	public int getFormatVersion() {
		return formatVersion;
	}


	public LocalDateTime getSavedAt() {
		return savedAt;
	}


	public String getCreator() {
		return creator;
	}


	public String getProB2KernelVersion() {
		return proB2KernelVersion;
	}


	public String getProBCliVersion() {
		return proBCliVersion;
	}


	public String getName() {
		return name;
	}


}
