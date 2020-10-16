package de.prob.animator.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.scripting.ModelTranslationError;
import org.apache.groovy.util.Maps;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public class CompareTwoOperationsTest {


	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager(){
		if(proBKernelStub==null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}
	}
	
	
	@Test
	public void two_identical_operations_test() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithOneOperation.mch"));

		CompoundPrologTerm compoundPrologTerm1 = new CompoundPrologTerm("b",
				new CompoundPrologTerm("operation", new CompoundPrologTerm("inccc"), new ListPrologTerm(), new ListPrologTerm(),
						new CompoundPrologTerm("b",
								new CompoundPrologTerm("assign_single_id",
										new CompoundPrologTerm("b",
												new CompoundPrologTerm("identifier",
														new CompoundPrologTerm("floors")),new CompoundPrologTerm("integer"), new ListPrologTerm()),

										new CompoundPrologTerm("b",
												new CompoundPrologTerm("identifier",
														new CompoundPrologTerm("floors")), new CompoundPrologTerm("integer"), new ListPrologTerm())), new CompoundPrologTerm("subst"), new ListPrologTerm())));


		CompareTwoOperations compareTwoOperations = new CompareTwoOperations(compoundPrologTerm1, compoundPrologTerm1, new ObjectMapper());
		proBKernelStub.executeCommand(compareTwoOperations);
		System.out.println(compareTwoOperations.getDelta());
		Assert.assertEquals(1, compareTwoOperations.getDelta().size());
		Map<String, String> expected = Collections.singletonMap("inccc", "inccc");
		Assert.assertEquals(expected, compareTwoOperations.getDelta());
	}
	
}
