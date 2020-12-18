package de.prob.json;

import java.time.Instant;

import de.prob.Main;

public final class JsonMetadataBuilder {

	private String fileType;
	private int formatVersion;
	private Instant savedAt = null;
	private String creator = null;
	private String proB2KernelVersion;
	private String proBCliVersion = null;
	private String modelName = null;

	public JsonMetadataBuilder(final String fileType, final int formatVersion) {
		this.fileType = fileType;
		this.formatVersion = formatVersion;
		this.proB2KernelVersion = Main.getVersion();
	}

	public JsonMetadataBuilder(final JsonMetadata metadata) {
		this.fileType = metadata.getFileType();
		this.formatVersion = metadata.getFormatVersion();
		this.savedAt = metadata.getSavedAt();
		this.creator = metadata.getCreator();
		this.proB2KernelVersion = metadata.getProB2KernelVersion();
		this.proBCliVersion = metadata.getProBCliVersion();
		this.modelName = metadata.getModelName();
	}

	public JsonMetadataBuilder withFileType(final String fileType) {
		this.fileType = fileType;
		return this;
	}

	public JsonMetadataBuilder withFormatVersion(final int formatVersion) {
		this.formatVersion = formatVersion;
		return this;
	}

	public JsonMetadataBuilder withSavedAt(final Instant savedAt) {
		this.savedAt = savedAt;
		return this;
	}

	public JsonMetadataBuilder withCreator(final String creator) {
		this.creator = creator;
		return this;
	}

	public JsonMetadataBuilder withProB2KernelVersion(final String proB2KernelVersion) {
		this.proB2KernelVersion = proB2KernelVersion;
		return this;
	}

	public JsonMetadataBuilder withProBCliVersion(final String proBCliVersion) {
		this.proBCliVersion = proBCliVersion;
		return this;
	}

	public JsonMetadataBuilder withModelName(final String modelName) {
		this.modelName = modelName;
		return this;
	}

	/**
	 * Shorthand for setting the built metadata's {@code creator} to {@link JsonMetadata#USER_CREATOR}.
	 *
	 * @return {@code this}
	 */
	public JsonMetadataBuilder withUserCreator() {
		return this.withCreator(JsonMetadata.USER_CREATOR);
	}

	/**
	 * Shorthand for setting the built metadata's {@code savedAt} to the current time.
	 *
	 * @return {@code this}
	 */
	public JsonMetadataBuilder withSavedNow() {
		return this.withSavedAt(Instant.now());
	}

	public JsonMetadata build() {
		return new JsonMetadata(
				this.fileType,
				this.formatVersion,
				this.savedAt,
				this.creator,
				this.proB2KernelVersion,
				this.proBCliVersion,
				this.modelName
		);
	}

}
