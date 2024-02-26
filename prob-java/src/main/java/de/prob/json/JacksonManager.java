package de.prob.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * <p>
 * Provides utilities for reading and writing JSON data with attached metadata,
 * in a way that correctly handles data from older and newer format versions.
 * </p>
 * <p>
 * Currently only supports working with files and not arbitrary readers/writers,
 * because the JSON data needs to be traversed multiple times when reading
 * (first to extract the format version, then again to read the actual data),
 * which is not always possible with arbitrary readers.
 * </p>
 */
public final class JacksonManager<T extends HasMetadata> {

	public static class Context<T extends HasMetadata> {

		protected final ObjectMapper objectMapper;
		protected final Class<T> clazz;
		protected final String fileType;
		protected final int currentFormatVersion;

		/**
		 * Initialize the context's required properties.
		 *
		 * @param objectMapper         the Jackson {@link ObjectMapper} to use to parse and serialize the JSON data
		 * @param clazz                the class to which the JSON root object should be mapped
		 * @param fileType             a string uniquely identifying the type of JSON data
		 * @param currentFormatVersion the version number for the current version of this format - should be incremented whenever the format changes in a way that previous versions of the code cannot read it anymore
		 */
		public Context(final ObjectMapper objectMapper, final Class<T> clazz, final String fileType, final int currentFormatVersion) {
			this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
			initObjectMapper(this.objectMapper);
			this.clazz = Objects.requireNonNull(clazz, "clazz");
			this.fileType = Objects.requireNonNull(fileType, "fileType");
			this.currentFormatVersion = currentFormatVersion;
		}

		/**
		 * Whether to accept JSON data with old or no metadata, as produced by ProB 2 UI 1.0 and earlier.
		 * Returns {@code false} by default.
		 * <p>
		 * This method only exists to support loading data from old ProB 2 UI versions.
		 * For new data formats, this method should <i>not</i> be used.
		 * For existing data formats that need to support old files with old/no metadata (e. g. trace files),
		 * this method can be overridden to return {@code true}.
		 *
		 * @return whether to accept JSON data with old or no metadata
		 */
		public boolean shouldAcceptOldMetadata() {
			return false;
		}

		/**
		 * <p>Convert data from an older format version to the current version.</p>
		 * <p>This method must be overridden to support loading data that uses an older format version. The default implementation of this method always throws a {@link JsonConversionException}.</p>
		 * <p>The converted object is returned from this method. The returned {@link JsonNode} may be a completely new object, or it may be {@code oldObject} after being modified in place.</p>
		 *
		 * @param oldObject  the old data to convert
		 * @param oldVersion the old data's format version (or 0 if the data has old metadata with no file type and format version)
		 * @return the converted data
		 * @throws JsonConversionException if the data could not be converted
		 */
		public ObjectNode convertOldData(final ObjectNode oldObject, final int oldVersion) {
			throw new JsonConversionException("JSON data uses old format version " + oldVersion + ", which cannot be converted to the current version " + this.currentFormatVersion);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(JacksonManager.class);

	private static final JsonMetadata MISSING_METADATA = new JsonMetadata(null, 0, null, null, null, null, null);

	// Only used for parsing
	private static final ObjectMapper METADATA_OBJECT_MAPPER = new ObjectMapper();

	static {
		initObjectMapper(METADATA_OBJECT_MAPPER);
	}

	private JacksonManager.Context<T> context;

	@Inject
	private JacksonManager() {
		super();

		this.context = null;
	}

	public JacksonManager.Context<T> getContext() {
		if (this.context == null) {
			throw new IllegalStateException("context not set");
		}
		return this.context;
	}

	/**
	 * Set the context for this {@link JacksonManager}. This method can only be called once per instance (the context cannot be changed or replaced afterwards).
	 *
	 * @param context the context to use
	 */
	public void initContext(final JacksonManager.Context<T> context) {
		if (this.context != null) {
			throw new IllegalStateException("context can only be set once");
		}
		this.context = Objects.requireNonNull(context, "context");
	}

	/**
	 * Initialize an {@link ObjectMapper} with default settings
	 * to ensure that JSON files are formatted consistently
	 * and the metadata part is read and written correctly.
	 *
	 * @param objectMapper the object mapper to initialize
	 */
	private static void initObjectMapper(final ObjectMapper objectMapper) {
		objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.setDefaultPrettyPrinter(new GsonStylePrettyPrinter());
		objectMapper.registerModule(new ParameterNamesModule());
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(new JavaTimeModule());
	}

	private static JsonMetadata extractNewMetadata(final JsonParser parser) throws IOException {
		return METADATA_OBJECT_MAPPER.readValue(parser, ObjectWithJustMetadata.class).getMetadata();
	}

	private static ProB2UI1Dot0Metadata extractOldMetadataIfPresent(final JsonParser parser) throws IOException {
		final JsonToken firstMetadataToken = parser.nextToken();
		if (firstMetadataToken == null) {
			// No old metadata present after the first top-level object.
			return null;
		} else if (firstMetadataToken == JsonToken.START_OBJECT) {
			return METADATA_OBJECT_MAPPER.readValue(parser, ProB2UI1Dot0Metadata.class);
		} else {
			throw new InvalidJsonFormatException("Expected data after top-level object to be another object, but got " + firstMetadataToken);
		}
	}

	private JsonMetadata readAndCheckMetadata(final JsonParser parser) throws IOException {
		// Try to read metadata in the new format.
		final JsonMetadata newMetadata = extractNewMetadata(parser);

		if (newMetadata != null) {
			// Found metadata in the new format.
			LOGGER.trace("Found metadata in new format: {}", newMetadata);
			LOGGER.debug("JSON data in file has type {} and version {}", newMetadata.getFileType(), newMetadata.getFormatVersion());

			// Check that the file has the expected type.
			if (!newMetadata.getFileType().equals(this.getContext().fileType)) {
				throw new InvalidJsonFormatException("Expected JSON data of type " + this.getContext().fileType + " but got " + newMetadata.getFileType());
			}

			return newMetadata;
		} else {
			LOGGER.trace("JSON data did not contain metadata in new format");
			// No new metadata found.
			// In this case, there is no explicit file type information in the JSON file.
			// This means that the file type cannot be checked automatically,
			// so make sure that the context wants/expects this.
			if (!this.getContext().shouldAcceptOldMetadata()) {
				throw new InvalidJsonFormatException("JSON data of type " + this.getContext().fileType + " requires new metadata format, but the loaded JSON file doesn't contain a \"metadata\" field");
			}

			// Try to read old ProB 2 UI 1.0 metadata,
			// which is stored directly behind the root object.
			final ProB2UI1Dot0Metadata oldMetadata = extractOldMetadataIfPresent(parser);
			if (oldMetadata != null) {
				// Found old metadata - convert it to the new format.
				return oldMetadata.toNewMetadata();
			} else {
				// Didn't find old metadata either.
				// This only happens for JSON data from pre-1.0 snapshot versions of ProB 2 UI
				// where metadata wasn't introduced yet.
				// Substitute a blank metadata object instead.
				return MISSING_METADATA;
			}
		}
	}

	/**
	 * Read an object from a JSON file.
	 * The file type and version number are checked against the settings in the context.
	 *
	 * @param path the path of the JSON file to read
	 * @return the read object
	 */
	public T readFromFile(final Path path) throws IOException {
		LOGGER.trace("Attempting to load JSON data of type {}, current version {}", this.getContext().fileType, this.getContext().currentFormatVersion);
		final JsonMetadata metadata;
		try (final JsonParser parserForMetadata = METADATA_OBJECT_MAPPER.createParser(path.toFile())) {
			metadata = this.readAndCheckMetadata(parserForMetadata);
		}

		T parsed;
		try (final JsonParser parserForRootObject = this.getContext().objectMapper.createParser(path.toFile())) {
			if (metadata.getFormatVersion() > this.getContext().currentFormatVersion) {
				// Check that the file format version isn't newer than we support.
				throw new InvalidJsonFormatException("JSON data uses format version " + metadata.getFormatVersion() + ", which is newer than the newest supported version (" + this.getContext().currentFormatVersion + ")");
			} else if (metadata.getFormatVersion() < this.getContext().currentFormatVersion) {
				// If the file format version is older than the current version,
				// parse it into a tree representation first,
				// then ask the context to update the tree representation to the current format
				// and finally parse the updated tree.
				LOGGER.info("Converting JSON data from old version {} to current version {}", metadata.getFormatVersion(), this.getContext().currentFormatVersion);
				final ObjectNode oldRootObject = this.getContext().objectMapper.readTree(parserForRootObject);
				final ObjectNode updatedRootObject = this.getContext().convertOldData(oldRootObject, metadata.getFormatVersion());
				parsed = this.getContext().objectMapper.treeToValue(updatedRootObject, this.getContext().clazz);
			} else {
				// If the file format version is equal to the current version,
				// no update/conversion is necessary,
				// so we can simply load the root object directly.
				assert metadata.getFormatVersion() == this.getContext().currentFormatVersion;
				parsed = this.getContext().objectMapper.readValue(parserForRootObject, this.getContext().clazz);
			}
		}

		// If the file didn't contain metadata in the new format,
		// the parsed object's metadata will still be null.
		// In that case we need to manually add the previously converted metadata to the object.
		if (parsed.getMetadata() == null) {
			assert metadata.getFormatVersion() == 0;
			parsed = this.getContext().clazz.cast(parsed.withMetadata(metadata));
		}

		return parsed;
	}

	/**
	 * Write an object to a JSON file.
	 * The file type and version number in the object's metadata must match the settings in the context.
	 *
	 * @param path   the path of the JSON file to write
	 * @param object the object to write
	 */
	public void writeToFile(final Path path, final T object) throws IOException {
		final JsonMetadata metadata = object.getMetadata();
		if (metadata == null) {
			throw new IllegalArgumentException("Object must have metadata set");
		}
		if (!this.getContext().fileType.equals(metadata.getFileType())) {
			throw new IllegalArgumentException(String.format(
				"File type in object metadata (%s) doesn't match file type set in context (%s)",
				metadata.getFileType(),
				this.getContext().fileType
			));
		}
		if (metadata.getFormatVersion() != this.getContext().currentFormatVersion) {
			throw new IllegalArgumentException(String.format(
				"Format version in object metadata (%d) doesn't match current format version set in context (%d)",
				metadata.getFormatVersion(),
				this.getContext().currentFormatVersion
			));
		}
		this.getContext().objectMapper.writeValue(path.toFile(), object);
	}
}
