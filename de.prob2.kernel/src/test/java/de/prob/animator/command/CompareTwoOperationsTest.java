package de.prob.animator.command;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import de.prob.ProBKernelStub;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.cli.CliTestCommon;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Deprecated
public class CompareTwoOperationsTest {
	private static ProBKernelStub proBKernelStub;

	@BeforeAll
	static void beforeAll() {
		proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
	}
	
	@AfterAll
	static void afterAll() {
		proBKernelStub.killCurrentAnimator();
	}
	
	@Test
	public void two_identical_operations_test() throws IOException {
		final StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch"));

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


		PrepareOperations prepareOperations1 = new PrepareOperations(compoundPrologTerm1);

		stateSpace.execute(prepareOperations1);

		CompareTwoOperations compareTwoOperations =
				new CompareTwoOperations(prepareOperations1.getPreparedOperation(), compoundPrologTerm1, prepareOperations1.getFreeVars());
		stateSpace.execute(compareTwoOperations);
		final Map<String, String> delta = TraceCheckerUtils.zip(PrologTerm.atomsToStrings(prepareOperations1.getFoundVars()), compareTwoOperations.getIdentifiers());
		Map<String, String> expected = new HashMap<>();
		expected.put("inccc", "inccc");
		expected.put("floors", "floors");
		Assertions.assertEquals(expected, delta);
	}


	@Test
	public void renamed_operations_test() throws IOException {
		final StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch"));

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

		CompoundPrologTerm compoundPrologTerm2 = new CompoundPrologTerm("b",
				new CompoundPrologTerm("operation", new CompoundPrologTerm("dinccc"), new ListPrologTerm(), new ListPrologTerm(),
						new CompoundPrologTerm("b",
								new CompoundPrologTerm("assign_single_id",
										new CompoundPrologTerm("b",
												new CompoundPrologTerm("identifier",
														new CompoundPrologTerm("floors")),new CompoundPrologTerm("integer"), new ListPrologTerm()),

										new CompoundPrologTerm("b",
												new CompoundPrologTerm("identifier",
														new CompoundPrologTerm("floors")), new CompoundPrologTerm("integer"), new ListPrologTerm())), new CompoundPrologTerm("subst"), new ListPrologTerm())));



		PrepareOperations prepareOperations1 = new PrepareOperations(compoundPrologTerm1);

		stateSpace.execute(prepareOperations1);

		CompareTwoOperations compareTwoOperations =
				new CompareTwoOperations(prepareOperations1.getPreparedOperation(), compoundPrologTerm2, prepareOperations1.getFreeVars());
		stateSpace.execute(compareTwoOperations);
		final Map<String, String> delta = TraceCheckerUtils.zip(PrologTerm.atomsToStrings(prepareOperations1.getFoundVars()), compareTwoOperations.getIdentifiers());
		Map<String, String> expected = new HashMap<>();
		expected.put("inccc", "dinccc");
		expected.put("floors", "floors");
		Assertions.assertEquals(expected, delta);
	}
	
}
