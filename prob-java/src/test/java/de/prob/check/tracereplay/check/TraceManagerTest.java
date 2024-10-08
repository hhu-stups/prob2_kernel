package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.json.JsonMetadata;
import de.prob.json.JsonMetadataBuilder;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TraceManagerTest {
	private static TraceManager traceManager;
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void serialize_correct_data_structure_test(@TempDir Path tempDir) throws IOException {

		Path tempDirPath = tempDir.resolve("testFile.txt");
		final StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch").toString());

		JsonMetadata metadata = new JsonMetadataBuilder("Trace", TraceJsonFile.CURRENT_FORMAT_VERSION)
			.withSavedNow()
			.withUserCreator()
			.withProBCliVersion("version")
			.withModelName("Lift")
			.build();
		TraceJsonFile abstractJsonFile = new TraceJsonFile(new Trace(stateSpace), metadata);
		traceManager.save(tempDirPath, abstractJsonFile);

		stateSpace.kill();
	}

	@Test
	public void serialize_correct_data_structure_test_2(@TempDir Path tempDir) throws IOException {

		Path tempDirPath = tempDir.resolve("testFile.txt");
		StateSpace stateSpace = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch").toString());

		JsonMetadata metadata = new JsonMetadataBuilder("Trace", TraceJsonFile.CURRENT_FORMAT_VERSION)
				.withSavedNow()
				.withUserCreator()
				.withProBCliVersion("version")
				.withModelName("Lift")
				.build();
		TraceJsonFile abstractJsonFile = new TraceJsonFile(new Trace(stateSpace), metadata);
		traceManager.save(tempDirPath, abstractJsonFile);

		stateSpace.kill();
	}

	@Test
	public void deserialize_correct_file_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));
	}

	@Test
	public void deserialize_10_steps_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));
	}

	@Test
	public void deserialize_file_wrong_field_test() {
		assertThrows(UnrecognizedPropertyException.class, () ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob","testmachines", "traces",  "test2.prob2trace")));
	}

	@Test
	public void deserialize_file_wrong_value_test() {
		assertThrows(JsonProcessingException.class, () ->
				traceManager.load(Paths.get("src", "test", "resources", "de", "prob","testmachines", "traces",  "test3.prob2trace")));
	}

	@Test
	public void deserialize_wrong_fields_test() {
		assertThrows(UnrecognizedPropertyException.class, () ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines","traces", "test4.prob2trace")));
	}
}
