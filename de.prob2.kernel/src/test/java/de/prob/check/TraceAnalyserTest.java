package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.PersistenceDelta;
import de.prob.check.tracereplay.check.TraceAnalyser;
import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class TraceAnalyserTest {

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

/*

	@Test
	public void integration_test_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("/home", "sebastian",  "master-thesis", "examples", "typeIV", "one_intermediate_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("/home", "sebastian",  "master-thesis", "examples", "typeIV", "one_intermediate_operation", "island.prob2trace"));

		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								jsonFile.getTrace().getTransitionList(),
								stateSpace,
								jsonFile.getMachineOperationInfos(),
								emptySet(),
								singleton("on"),
								emptySet(),
								emptySet(),
								emptySet());

		Map<String, TraceAnalyser.AnalyserResult> bla = TraceAnalyser.analyze(singleton("on"), result.values().stream().findFirst().get(), jsonFile.getTrace().getTransitionList());

		Assert.assertEquals(1, bla.entrySet().size());
		Assert.assertEquals(TraceAnalyser.AnalyserResult.Mixed, bla.get("on"));
	}


	@Test
	public void integration_test_always_new_intermediate_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("/home", "sebastian",  "master-thesis", "examples", "typeIV", "always_intermediate", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("/home", "sebastian",  "master-thesis", "examples", "typeIV", "always_intermediate", "island.prob2trace"));


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								jsonFile.getTrace().getTransitionList(),
								stateSpace,
								jsonFile.getMachineOperationInfos(),
								emptySet(),
								singleton("on"),
								emptySet(),
								emptySet(),
								emptySet());

		Map<String, TraceAnalyser.AnalyserResult> bla = TraceAnalyser.analyze(singleton("on"), result.values().stream().findFirst().get(), jsonFile.getTrace().getTransitionList());

		Assert.assertEquals(1, bla.entrySet().size());
		Assert.assertEquals(TraceAnalyser.AnalyserResult.Intermediate, bla.get("on"));
	}


	@Test
	public void integration_test_complete_renamed_operation_1() throws IOException, ModelTranslationError {
		StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("/home", "sebastian",  "master-thesis", "examples", "typeIV", "complete_renamed_operation", "ISLAND2.mch"));

		TraceJsonFile jsonFile = traceManager.load(Paths.get("/home", "sebastian",  "master-thesis", "examples", "typeIV", "complete_renamed_operation", "island.prob2trace"));


		Map<Map<String, Map<String, String>>, List<PersistenceDelta>> result =
				new TraceExplorer(false, new TestUtils.StubFactoryImplementation())
						.replayTrace(
								jsonFile.getTrace().getTransitionList(),
								stateSpace,
								jsonFile.getMachineOperationInfos(),
								emptySet(),
								singleton("on"),
								emptySet(),
								emptySet(),
								emptySet());

		Map<String, TraceAnalyser.AnalyserResult> bla = TraceAnalyser.analyze(singleton("on"), result.values().stream().findFirst().get(), jsonFile.getTrace().getTransitionList());

		Assert.assertEquals(1, bla.entrySet().size());
		Assert.assertEquals(TraceAnalyser.AnalyserResult.Straight, bla.get("on"));
	}
*/


}
