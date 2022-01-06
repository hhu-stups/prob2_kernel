package de.prob.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utilities for reading and writing JSON data with attached metadata, in a way that correctly handles data from older and newer UI versions.
 */
public final class JsonManager<T> {

	public static class Context<T> {
		protected final Gson gson;
		protected final Class<T> clazz;
		protected final String fileType;
		protected final int currentFormatVersion;

		public Context(final Gson gson, final Class<T> clazz, final String fileType, final int currentFormatVersion) {
			this.gson = Objects.requireNonNull(gson, "gson");
			this.clazz = Objects.requireNonNull(clazz, "clazz");
			this.fileType = Objects.requireNonNull(fileType, "fileType");
			this.currentFormatVersion = currentFormatVersion;
		}
		
		public JsonMetadataBuilder getDefaultMetadataBuilder() {
			return new JsonMetadataBuilder(this.fileType, this.currentFormatVersion)
					.withSavedNow()
					.withUserCreator();
		}

		/**
		 * <p>Convert data from an older format version to the current version.</p>
		 * <p>This method must be overridden to support loading data that uses an older format version. The default implementation of this method always throws a {@link JsonParseException}.</p>
		 * <p>The converted object and metadata are returned from this method. The returned {@link JsonObject} may be a completely new object, or it may be {@code oldObject} after being modified in place. The returned {@link JsonMetadata} does <i>not</i> need to have its version number updated.</p>
		 *
		 * @param oldObject the old data to convert
		 * @param oldMetadata the metadata attached to the old data
		 * @return the converted data and updated metadata
		 * @throws JsonParseException if the data could not be converted
		 */
		public ObjectWithMetadata<JsonObject> convertOldData(final JsonObject oldObject, final JsonMetadata oldMetadata) {
			throw new JsonParseException("JSON data uses old format version " + oldMetadata.getFormatVersion() + ", which cannot be converted to the current version " + this.currentFormatVersion);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonManager.class);

	private JsonManager.Context<T> context;

	@Inject
	private JsonManager() {
		super();

		this.context = null;
	}

	public static JsonElement checkGet(final JsonObject object, final String memberName) {
		final JsonElement value = object.get(memberName);
		if (value == null) {
			throw new JsonParseException("Missing required field " + memberName);
		}
		return value;
	}

	public static <T> T checkDeserialize(final JsonDeserializationContext context, final JsonObject object, final String memberName, final Type typeOfT) {
		final T deserialized = context.deserialize(checkGet(object, memberName), typeOfT);
		if (deserialized == null) {
			throw new JsonParseException("Value of field " + memberName + " is null or invalid");
		}
		return deserialized;
	}

	public static <T> T checkDeserialize(final JsonDeserializationContext context, final JsonObject object, final String memberName, final Class<T> classOfT) {
		return checkDeserialize(context, object, memberName, (Type)classOfT);
	}

	public JsonManager.Context<T> getContext() {
		if (this.context == null) {
			throw new IllegalStateException("context not set");
		}
		return this.context;
	}

	/**
	 * Set the context for this {@link JsonManager}. This method can only be called once per instance (the context cannot be changed or replaced afterwards).
	 *
	 * @param context the context to use
	 */
	public void initContext(final JsonManager.Context<T> context) {
		if (this.context != null) {
			throw new IllegalStateException("context can only be set once");
		}
		this.context = Objects.requireNonNull(context, "context");
	}

	/**
	 * Create a builder for a new {@link JsonMetadata} object. The file type and version are initialized based on the settings in the context.
	 *
	 * @return a builder for a new {@link JsonMetadata object}
	 */
	public JsonMetadataBuilder metadataBuilder() {
		return new JsonMetadataBuilder(this.getContext().fileType, this.getContext().currentFormatVersion);
	}

	/**
	 * Create a builder for a {@link JsonMetadata} object based on an existing metadata object.
	 *
	 * @param metadata an existing {@link JsonMetadata} object used to initialize this builder
	 *
	 * @return a builder for a {@link JsonMetadata} object based on an existing metadata object
	 */
	public JsonMetadataBuilder metadataBuilder(final JsonMetadata metadata) {
		return new JsonMetadataBuilder(metadata);
	}

	/**
	 * Create a builder for a {@link JsonMetadata} object with default settings. The builder may be customized by overriding {@link JsonManager.Context#getDefaultMetadataBuilder()} in the context.
	 *
	 * @return a builder for a {@link JsonMetadata} object with default settings
	 */
	public JsonMetadataBuilder defaultMetadataBuilder() {
		return this.getContext().getDefaultMetadataBuilder();
	}

	/**
	 * Read an object along with its metadata from the JSON data in the reader. The file type and version number are checked against the settings in the context.
	 *
	 * @param reader the {@link Reader} from which to read the JSON data
	 * @return the read object along with its metadata
	 */
	public ObjectWithMetadata<T> read(final Reader reader) {
		LOGGER.trace("Attempting to load JSON data of type {}, current version {}", this.getContext().fileType, this.getContext().currentFormatVersion);
		final ObjectWithMetadata<JsonObject> rawWithMetadata = JsonManagerRaw.readRaw(reader);
		JsonObject rawObject = rawWithMetadata.getObject();
		JsonMetadata metadata = rawWithMetadata.getMetadata();
		LOGGER.trace("Found JSON data of type {}, version {}", metadata.getFileType(), metadata.getFormatVersion());

		if (metadata.getFileType() != null && !metadata.getFileType().equals(this.getContext().fileType)) {
			throw new JsonParseException("Expected JSON data of type " + this.getContext().fileType + " but got " + metadata.getFileType());
		}

		if (metadata.getFormatVersion() > this.getContext().currentFormatVersion) {
			throw new JsonParseException("JSON data uses format version " + metadata.getFormatVersion() + ", which is newer than the newest supported version (" + this.getContext().currentFormatVersion + ")");
		}

		if (metadata.getFormatVersion() < this.getContext().currentFormatVersion) {
			LOGGER.info("Converting JSON data from old version {} to current version {}", metadata.getFormatVersion(), this.getContext().currentFormatVersion);
			final ObjectWithMetadata<JsonObject> converted = this.getContext().convertOldData(rawObject, metadata);
			rawObject = converted.getObject();
			metadata = converted.getMetadata();
		}
		final T obj = this.getContext().gson.fromJson(rawObject, this.getContext().clazz);

		return new ObjectWithMetadata<>(obj, metadata);
	}

	/**
	 * Read an object along with its metadata from a JSON file. The file type and version number are checked against the settings in the context.
	 *
	 * @param path the path of the JSON file to read
	 * @return the read object along with its metadata
	 */
	public ObjectWithMetadata<T> readFromFile(final Path path) throws IOException {
		try (final Reader reader = Files.newBufferedReader(path)) {
			return this.read(reader);
		}
	}

	/**
	 * Write an object as JSON to the writer, along with the provided metadata.
	 *
	 * @param writer the {@link Writer} to which to write the JSON data
	 * @param src the object to write
	 * @param metadata the metadata to attach to the JSON data
	 */
	public void write(final Writer writer, final T src, final JsonMetadata metadata) {
		JsonManagerRaw.writeRaw(writer, this.getContext().gson.toJsonTree(src).getAsJsonObject(), metadata);
	}

	/**
	 * Write an object as JSON to the writer, along with default metadata built using {@link #defaultMetadataBuilder()}.
	 *
	 * @param writer the {@link Writer} to which to write the JSON data
	 * @param src the object to write
	 */
	public void write(final Writer writer, final T src) {
		JsonManagerRaw.writeRaw(writer, this.getContext().gson.toJsonTree(src).getAsJsonObject(), this.defaultMetadataBuilder().build());
	}

	/**
	 * Write an object to a JSON file, along with the provided metadata.
	 *
	 * @param path the path of the JSON file to write
	 * @param src the object to write
	 * @param metadata the metadata to attach to the JSON data
	 */
	public void writeToFile(final Path path, final T src, final JsonMetadata metadata) throws IOException {
		try (final Writer writer = Files.newBufferedWriter(path)) {
			this.write(writer, src, metadata);
		}
	}

	/**
	 * Write an object to a JSON file, along with default metadata built using {@link #defaultMetadataBuilder()}.
	 *
	 * @param path the path of the JSON file to write
	 * @param src the object to write
	 */
	public void writeToFile(final Path path, final T src) throws IOException {
		try (final Writer writer = Files.newBufferedWriter(path)) {
			this.write(writer, src, this.defaultMetadataBuilder().build());
		}
	}

}
