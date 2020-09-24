package de.prob.check.tracereplay.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type"
)
@JsonSubTypes(
		@JsonSubTypes.Type(value = MetaData.class, name = "TraceMetaData")
)
public abstract class AbstractMetaData {

	private int formatVersion;
	private LocalDate savedAt;
	private String creator;
	private String proB2KernelVersion;
	private String proBCliVersion;
	private String name;


	public int getFormatVersion() {
		return formatVersion;
	}

	public void setFormatVersion(int formatVersion) {
		this.formatVersion = formatVersion;
	}

	public LocalDate getSavedAt() {
		return savedAt;
	}

	public void setSavedAt(LocalDate savedAt) {
		this.savedAt = savedAt;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getProB2KernelVersion() {
		return proB2KernelVersion;
	}

	public void setProB2KernelVersion(String proB2KernelVersion) {
		this.proB2KernelVersion = proB2KernelVersion;
	}

	public String getProBCliVersion() {
		return proBCliVersion;
	}

	public void setProBCliVersion(String proBCliVersion) {
		this.proBCliVersion = proBCliVersion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
