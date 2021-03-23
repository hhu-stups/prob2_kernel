package de.prob.animator.command;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.ProBKernelStub;
import de.prob.cli.CliTestCommon;
import de.prob.statespace.StateSpace;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PrepareOperationsTest {
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
	public void get_prepared_operation_test() throws IOException {
		final StateSpace stateSpace = proBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch"));

		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();

		stateSpace.execute(getMachineOperationsFull);

		PrepareOperations prepareOperations = new PrepareOperations(getMachineOperationsFull.getOperationsWithNames().get("on"));

		stateSpace.execute(prepareOperations);


		Assert.assertEquals(3, prepareOperations.getFreeVars().size());
		Assert.assertEquals(3, prepareOperations.getFoundVars().size());
		Assert.assertEquals("maxCars", prepareOperations.getFoundVars().get(0).getFunctor());
		Assert.assertEquals("cars", prepareOperations.getFoundVars().get(1).getFunctor());
		Assert.assertEquals("on", prepareOperations.getFoundVars().get(2).getFunctor());

	}


}
