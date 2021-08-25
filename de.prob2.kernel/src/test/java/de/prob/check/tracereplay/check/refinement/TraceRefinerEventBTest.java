package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TraceRefinerEventBTest {
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
	public void simple_event_b_no_changes() throws IOException, TraceConstructionError {


		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "trafficLight", "mac.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB",  "trafficLight", "test1234.prob2trace"));


		List<PersistentTransition> result = new TraceRefinerEventB(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1).refineTrace();

		Assertions.assertEquals(jsonFile.getTransitionList().size(), result.size());
	}





	@Test
	public void simple_event_b_refinement_success() throws IOException, TraceConstructionError {

		Path pathStateSpace1 = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "trafficLight", "mac1.bum");

		TraceJsonFile jsonFile = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB",  "trafficLight", "test1234.prob2trace"));

		List<PersistentTransition> result = new TraceRefinerEventB(CliTestCommon.getInjector(), jsonFile.getTransitionList(), pathStateSpace1).refineTrace();


		String comparison1 = "activateSystem";


		List<String> nameList = result.stream().map(PersistentTransition::getOperationName).collect(Collectors.toList());
		List<String> nameListWithoutSkip = nameList.stream().filter(entry -> !entry.equals(comparison1)).collect(Collectors.toList());


		boolean hasActivatedTheSystemOnce = nameList.stream().filter(entry -> entry.equals(comparison1)).count() == 1;

		Assertions.assertTrue(hasActivatedTheSystemOnce);
		Assertions.assertEquals(11, nameListWithoutSkip.size());

	}

}
