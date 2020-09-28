package de.prob.check.json;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.check.tracereplay.PersistentTrace;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.AbstractJsonFile;
import de.prob.check.tracereplay.json.storage.AbstractMetaData;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.check.tracereplay.json.storage.TraceMetaData;
import de.prob.statespace.LoadedMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;

public class TraceManagerTest {

	TraceManager traceManager = null;
	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(traceManager==null && proBKernelStub==null) {
			Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new JsonManagerStubModule());
			this.traceManager = injector.getInstance(TraceManager.class);
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}
		
	}
	
	
	@Test
	public void deserialize_correct_file_test() throws IOException {
		traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "traces", "UnkownLift.prob2trace"));
	}


	@Test
	public void serialize_correct_data_structure_test() throws IOException {
		LoadedMachine loadedMachine = proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch"));
		AbstractMetaData abstractMetaData = new TraceMetaData(1, LocalDate.now(), "User", "version", "bla");
		PersistentTrace persistentTrace = proBKernelStub.getATrace();
		AbstractJsonFile abstractJsonFile = new TraceJsonFile("testFile", "description", persistentTrace, loadedMachine, abstractMetaData);
		traceManager.save(Paths.get("src", "test", "resources", "de", "prob", "traces", "UnkownLift.prob2trace"), abstractJsonFile);
	}
}
