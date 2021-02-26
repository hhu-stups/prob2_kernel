package de.prob.check;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.AbstractJsonFile;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.json.JsonMetadata;
import de.prob.json.JsonMetadataBuilder;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TraceManagerTest {
	private static TraceManager traceManager;
	private static ProBKernelStub proBKernelStub;

	@BeforeAll
	static void beforeAll() {
		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
		proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}

	@AfterAll
	static void afterAll() {
		proBKernelStub.killCurrentAnimator();
	}

	@Test
	public void serialize_correct_data_structure_test(@TempDir Path tempDir) throws IOException, ModelTranslationError {

		Path tempDirPath = tempDir.resolve("testFile.txt");
		final StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch"));

		JsonMetadata metadata = new JsonMetadataBuilder("Trace", 2)
			.withSavedNow()
			.withUserCreator()
			.withProBCliVersion("version")
			.withModelName("Lift")
			.build();
		PersistentTrace persistentTrace = new PersistentTrace(new Trace(stateSpace));
		AbstractJsonFile abstractJsonFile = new TraceJsonFile("testFile", "description", persistentTrace, stateSpace.getLoadedMachine(), metadata);
		traceManager.save(tempDirPath, abstractJsonFile);
	}


	@Test
	public void deserialize_correct_file_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "trace6.prob2trace"));
	}


	@Test
	public void deserialize_file_wrong_value_test()  {
		assertThrows(JsonMappingException.class,() ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "test1.prob2trace")));
	}

	@Test
	public void deserialize_file_wrong_field_test() {
		assertThrows(UnrecognizedPropertyException.class, () ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob","testmachines", "traces",  "test2.prob2trace")));
	}

	@Test
	public void deserialize_file_missing_field_test() {
		assertThrows(ValueInstantiationException.class, () ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines","traces", "test4.prob2trace")));
	}


	@Test
	public void deserialize_10_steps_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine10Steps.prob2trace"));
	}




}