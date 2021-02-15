package de.prob.check;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.JsonManagerStubModule;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.VariableTypeFinder;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class VariableTypeFinderTest {

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

	@AfterEach
	public void cleanUp(){
		proBKernelStub.killCurrentAnimator();
	}

	@Test
	public void usedVariables_test() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));

		Set<String> result = VariableTypeFinder.usedVariables(bla.getTrace());

		Set<String> expected = new HashSet<>();
		expected.add("floors");
		Assertions.assertEquals(expected, result);
	}


	@Test
	public void getUsedVarsOfTransition_test() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));

		Set<String> result = VariableTypeFinder.getUsedVarsOfTransition(bla.getTrace().getTransitionList().get(0));

		Set<String> expected = new HashSet<>();
		expected.add("floors");
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void integration_test_1() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));

		Set<String> first = new HashSet<>();
		first.add("floors");

		Set<String> second = new HashSet<>();
		second.add("level");

		VariableTypeFinder variableTypeFinder = new VariableTypeFinder(bla.getTrace(), first, second);


		Set<String> expected = new HashSet<>();
		expected.add("floors");


		Assertions.assertEquals(expected, variableTypeFinder.getTypeIIorIVCandidates());
		Assertions.assertEquals(new HashSet<String>(), variableTypeFinder.getTypeICandidates());
	}


	@Test
	public void integration_test_2() throws IOException {


		TraceJsonFile bla = traceManager.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "changedTypeIIandTypeIII", "LiftProto.prob2trace"));

		Set<String> first = new HashSet<>();
		first.add("floors");

		Set<String> second = new HashSet<>();
		second.add("floors");

		VariableTypeFinder variableTypeFinder = new VariableTypeFinder(bla.getTrace(), first, second);


		Set<String> expected = new HashSet<>();
		expected.add("floors");


		Assertions.assertEquals(expected, variableTypeFinder.getTypeICandidates());
		Assertions.assertEquals(new HashSet<String>(), variableTypeFinder.getTypeIIorIVCandidates());
	}


}
