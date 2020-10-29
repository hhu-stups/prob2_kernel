package de.prob.animator.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.scripting.ModelTranslationError;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class PrepareOperationsTest {

	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager() {
		if (proBKernelStub == null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}
	}


	/**
	 * Integration test. Functionality is tested in prolog
	 * @throws IOException
	 * @throws ModelTranslationError
	 */
	@Test
	public void get_prepared_operation_test() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "machineWithOneOperation.mch"));

		GetMachineOperationsFull getMachineOperationsFull = new GetMachineOperationsFull();

		proBKernelStub.executeCommand(getMachineOperationsFull);

		System.out.println(getMachineOperationsFull.getOperationsWithNames());
		PrepareOperations prepareOperations = new PrepareOperations(getMachineOperationsFull.getOperationsWithNames().get("inccc"));

		proBKernelStub.executeCommand(prepareOperations);

		Assert.assertEquals(1, prepareOperations.getFreeVars().size());
		Assert.assertEquals(1, prepareOperations.getFoundVars().size());

	}

}
