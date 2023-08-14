package de.prob.json;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Old version of {@link JsonMetadata} used in files saved by ProB 2 UI version 1.0.
 * This class is only used internally,
 * to parse the old metadata format and convert it to the new format.
 * External code always uses the new {@link JsonMetadata} representation.
 */
final class ProB2UI1Dot0Metadata {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProB2UI1Dot0Metadata.class);
	
	private static final DateTimeFormatter OLD_METADATA_DATE_FORMATTER = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.parseLenient()
			.appendPattern("d MMM yyyy hh:mm:ssa O")
			.toFormatter();
	
	final String creationDate;
	final String createdBy;
	final String proB2KernelVersion;
	final String proBCliVersion;
	final String model;
	
	@JsonCreator
	private ProB2UI1Dot0Metadata(
		@JsonProperty("Creation Date") final String creationDate,
		@JsonProperty("Created by") final String createdBy,
		@JsonProperty("ProB 2.0 kernel Version") final String proB2KernelVersion,
		@JsonProperty("ProB CLI Version") final String proBCliVersion,
		@JsonProperty("Model") final String model
	) {
		this.creationDate = creationDate;
		this.createdBy = createdBy;
		this.proB2KernelVersion = Objects.requireNonNull(proB2KernelVersion, "ProB 2.0 kernel Version");
		this.proBCliVersion = Objects.requireNonNull(proBCliVersion, "ProB CLI Version");
		this.model = model;
	}
	
	JsonMetadata toNewMetadata() {
		Instant creationDateTime;
		if (this.creationDate != null) {
			try {
				creationDateTime = OLD_METADATA_DATE_FORMATTER.parse(this.creationDate, Instant::from);
			} catch (DateTimeParseException e) {
				LOGGER.warn("Failed to parse creation date from ProB 2 UI 1.0 metadata, replacing with null", e);
				creationDateTime = null;
			}
		} else {
			creationDateTime = null;
		}
		
		return new JsonMetadata(
			null,
			0,
			creationDateTime,
			this.createdBy,
			this.proB2KernelVersion,
			this.proBCliVersion,
			this.model
		);
	}
}
