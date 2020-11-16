package de.prob.check.json;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.AbstractJsonFile;
import de.prob.check.tracereplay.json.storage.AbstractMetaData;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.check.tracereplay.json.storage.TraceMetaData;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.LoadedMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TraceManagerTest {

	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}
		
	}

	@Test
	public void serialize_correct_data_structure_test() throws IOException, ModelTranslationError {



		LoadedMachine loadedMachine = proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch"));

		AbstractMetaData abstractMetaData = new TraceMetaData(1, LocalDateTime.now(), "User", "version", "bla", "");
		PersistentTrace persistentTrace = proBKernelStub.getATrace();
		AbstractJsonFile abstractJsonFile = new TraceJsonFile("testFile", "description", persistentTrace, loadedMachine, abstractMetaData);
		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "traces", "test6.prob2trace"), abstractJsonFile);
	}


	@Test
	public void mega() throws IOException, ModelTranslationError {


		LoadedMachine loadedMachine = proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "testTraceMachine.mch"));

		AbstractMetaData abstractMetaData = new TraceMetaData(1, LocalDateTime.now(), "User", "version", "bla", "");
		PersistentTrace persistentTrace = proBKernelStub.getATrace();
		AbstractJsonFile abstractJsonFile = new TraceJsonFile("testFile", "description", persistentTrace, loadedMachine, abstractMetaData);
		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "traces", "test6.prob2trace"), abstractJsonFile);
	}


	@Test
	public void deserialize_correct_file_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "trace6.prob2trace"));
	}


	@Test
	public void deserialize_file_wrong_value_test()  {
		assertThrows(JsonMappingException.class,() ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "test1.prob2trace")));
	}

	@Test
	public void deserialize_file_wrong_field_test() {
		assertThrows(UnrecognizedPropertyException.class, () ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "test2.prob2trace")));
	}

	@Test
	public void deserialize_file_missing_field_test() {
		assertThrows(ValueInstantiationException.class, () ->
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "test4.prob2trace")));
	}


	@Test
	public void deserialize_10_steps_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "testTraceMachine10Steps.prob2trace"));
	}




}
