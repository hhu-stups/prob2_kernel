package de.prob.json;

import java.time.Instant;

public final class JsonMetadataBuilder {

    private String fileType;
    private int formatVersion;
    private Instant savedAt = null;
    private String creator = null;
    private String proB2KernelVersion = null;
    private String proBCliVersion = null;
    private String modelName = null;

    public JsonMetadataBuilder(final String fileType, final int formatVersion, final String proB2KernelVersion,
                               final String proBCliVersion, final String modelName) {
        this.fileType = fileType;
        this.formatVersion = formatVersion;
        this.proB2KernelVersion = proB2KernelVersion;
        this.proBCliVersion = proBCliVersion;
        this.modelName = modelName;
    }

    public JsonMetadataBuilder(final String fileType, final int formatVersion, final String proB2KernelVersion) {
        this.fileType = fileType;
        this.formatVersion = formatVersion;
        this.proB2KernelVersion = proB2KernelVersion;
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

    /**
     * Shorthand for setting the built metadata's {@code proB2KernelVersion} to the version currently in use.
     *
     * @return {@code this}
     */
    public JsonMetadataBuilder withCurrentProB2KernelVersion() {
        return this.withProB2KernelVersion(this.proB2KernelVersion);
    }

    /**
     * Shorthand for setting the built metadata's {@code proBCliVersion} to the version currently in use.
     *
     * @return {@code this}
     */
    public JsonMetadataBuilder withCurrentProBCliVersion() {
        return this.withProBCliVersion(this.proBCliVersion);
    }

    /**
     * Shorthand for setting the built metadata's {@code savedAt} to the current time, and {@code proB2KernelVersion} and {@code proBCliVersion} to the versions currently in use.
     *
     * @return {@code this}
     */
    public JsonMetadataBuilder withCurrentInfo() {
        return this.withSavedNow()
                .withCurrentProB2KernelVersion()
                .withCurrentProBCliVersion();
    }

    /**
     * Shorthand for setting the built metadata's {@code modelName} to the name of the currently loaded machine.
     *
     * @return {@code this}
     */
    public JsonMetadataBuilder withCurrentModelName() {
        return this.withModelName(this.modelName);
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
