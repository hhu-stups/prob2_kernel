package de.prob.json;

import com.google.gson.*;
import com.google.inject.Inject;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.PersistentTransition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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
		
		/**
		 * @deprecated Use {@link #Context(Gson, Class, String, int)} with an explicit {@link Gson} parameter instead.
		 */
		@Deprecated
		public Context(final Class<T> clazz, final String fileType, final int currentFormatVersion) {
			this(new Gson(), clazz, fileType, currentFormatVersion);
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

		/*
		 * Check if a trace file is correct
		 * TODO check for any file if it is correct?
		 */

		LOGGER.info(this.getContext().clazz.getName());
		if(this.getContext().clazz == PersistentTrace.class)
		{
			checkTracFile(rawObject);
		}


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
	 * Runs several sanity checks for a given JsonObject
	 *
	 * @param rawObject the object to be checked
	 * @throws JsonParseException the object is somehow not valid
	 */
	private void checkTracFile(JsonObject rawObject){

		// PersistentTrace Object sanity check
		checkClassFieldsMatchJsonObject(context.clazz, rawObject);

		// PersistentTransitions Object sanity check
		JsonArray transitionList = rawObject.get("transitionList").getAsJsonArray();

		//Check if jsonObjects can be safely translated to real objects
		List<String> optionalFields = Arrays.asList("preds", "destStateNotChanged");

		for(JsonElement jsonElement : transitionList){
			checkClassFieldsMatchJsonObject(PersistentTransition.class, jsonElement.getAsJsonObject(), optionalFields);
			try {
				this.context.gson.fromJson(jsonElement.getAsJsonObject(), PersistentTransition.class);
			}catch(JsonSyntaxException e){
				throw new JsonParseException("\n The JSON file seems to be corrupted. A value in the transitionList couldn't be parsed. Location is " + jsonElement
						+ "\n\n" +  e.getMessage());
			}
		}
	}

	/**
	 * Same as checkClassFieldsMatchJsonObject(Class clazz, JsonObject jsonObject) but instead we can provide a list
	 * with fields that are not needed
	 * @param clazz the class to run the check against
	 * @param jsonObject the jsonObject in question
	 * @param optionalFields a list with fields that are part of clazz but being optional for a functioning trace
	 */
	public void checkClassFieldsMatchJsonObject(Class clazz, JsonObject jsonObject, List<String> optionalFields){
		Set<String> fieldsFromTarget = Arrays.stream(clazz.getDeclaredFields())
				.filter(field -> !field.isSynthetic()) //Filter fields that are generate via tests aká $jacocoData
				.map(Field::getName).collect(Collectors.toSet());
		Set<String> cp = new HashSet<>(fieldsFromTarget);
		for(String key : jsonObject.keySet())
		{
			if(!fieldsFromTarget.contains(key)){
				String closestMatch = findClosestMatch(key, new ArrayList<>(fieldsFromTarget));
				throw new JsonParseException("The JSON file seems to be corrupted. Searched for key <" + key +
						"> but available candidates are " + cp + ".\nDid you maybe meant <" + closestMatch + "> instead of <" + key + ">?\n" );
			}
			fieldsFromTarget.remove(key);
		}


		// Missing fields - gson is stupid an would parse it anyway leading to NPEs
		fieldsFromTarget.removeAll(optionalFields);

		if(!fieldsFromTarget.isEmpty()){
			throw new JsonParseException("The JSON file seems to be corrupted. Some fields needed for generating " + clazz.getSimpleName()
					+ " are not contained in the data found. Missing fields are: " + fieldsFromTarget + " at position: \n " + jsonObject);
		}
	}

	/**
	 * Checks if the fields of a given class matching the fields found in a json object
	 * @param clazz the class to run the check against
	 * @param jsonObject the jsonObject in question
	 * @throws JsonParseException the fields don´t match
	 */
	public void checkClassFieldsMatchJsonObject(Class clazz, JsonObject jsonObject) {
		checkClassFieldsMatchJsonObject(clazz, jsonObject, Collections.emptyList());
	}

	/**
	 * Finds the nearest neighbour in a list of string. If the list is empty a empty String is returned. The given list
	 * is transformed to a no fixed size list as fixed size list will lead to errors.
	 * @param target string we want to find the closest match
	 * @param candidates potential matches
	 * @return the closest match
	 */
	public String findClosestMatch(String target, List<String> candidates){
		if(candidates.size() == 0){
			return "";
		}
		List<String> list = new LinkedList<>(candidates);
		list.sort((o1, o2) -> {

			int levDistance1 = StringUtils.getLevenshteinDistance(target, o1);
			int levDistance2 = StringUtils.getLevenshteinDistance(target, o2);
			return levDistance1-levDistance2;
		});
		return list.get(0);
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
