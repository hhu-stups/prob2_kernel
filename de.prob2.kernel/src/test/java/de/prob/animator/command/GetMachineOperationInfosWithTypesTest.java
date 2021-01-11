package de.prob.animator.command;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.ProBKernelStub;
import de.prob.scripting.ModelTranslationError;
import org.apache.groovy.util.Maps;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class GetMachineOperationInfosWithTypesTest {

	ProBKernelStub proBKernelStub = null;

	@BeforeEach
	public void createJsonManager() {
		if (proBKernelStub == null) {
			System.setProperty("prob.home", "/home/sebastian/prob_prolog");
			Injector injector1 = Guice.createInjector(Stage.DEVELOPMENT, new MainModule());
			this.proBKernelStub = injector1.getInstance(ProBKernelStub.class);
		}
	}


	@Test
	public void get_prepared_operation_test() throws IOException, ModelTranslationError {
		proBKernelStub.load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "OneOperationMachine.mch"));

		GetMachineOperationInfosWithTypes getMachineOperationsFull = new GetMachineOperationInfosWithTypes();

		proBKernelStub.executeCommand(getMachineOperationsFull);

		Map<String, String> expected = Maps.of("x" , "integer", "y", "integer", "z", "boolean", "out", "integer");

		Assert.assertEquals(1, getMachineOperationsFull.getOperationInfos().size());
		Assert.assertEquals(expected, getMachineOperationsFull.getOperationInfos().get(0).getTypeMap());

	}

}
