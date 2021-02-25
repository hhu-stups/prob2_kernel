package de.prob.animator.command;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.ProBKernelStub;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ModelTranslationError;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrepareOperationsTest {

	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager() {
		if (proBKernelStub == null) {
			this.proBKernelStub = CliTestCommon.getInjector().getInstance(ProBKernelStub.class);
		}
	}


	@Test
	public void get_prepared_operation_test() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "examplesForOperations", "machineWithOneOperation.mch"));

		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();

		proBKernelStub.executeCommand(getMachineOperationsFull);

		PrepareOperations prepareOperations = new PrepareOperations(getMachineOperationsFull.getOperationsWithNames().get("on"));

		proBKernelStub.executeCommand(prepareOperations);


		Assert.assertEquals(3, prepareOperations.getFreeVars().size());
		Assert.assertEquals(3, prepareOperations.getFoundVars().size());
		Assert.assertEquals("maxCars", prepareOperations.getFoundVars().get(0).getFunctor());
		Assert.assertEquals("cars", prepareOperations.getFoundVars().get(1).getFunctor());
		Assert.assertEquals("on", prepareOperations.getFoundVars().get(2).getFunctor());

	}


}
