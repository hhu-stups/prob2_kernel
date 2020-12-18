package de.prob.cli.integration.commands;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.animator.command.GetMachineOperationInfos;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetMachineOperationInfosCommandTest {

	private Api api;
	private StateSpace s;

	@Before
	public void setupClass() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testGetMachineOperationInfosCommand() throws IOException, ModelTranslationError {
		s = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch").toString());
		assertNotNull(s);
		GetMachineOperationInfos command = new GetMachineOperationInfos();
		s.execute(command);
		OperationInfo operationInfo = command.getOperationInfos().get(0);
		assertEquals("Foo", operationInfo.getOperationName());
		assertEquals("p1", operationInfo.getParameterNames().get(0));
		assertEquals("p2", operationInfo.getParameterNames().get(1));
		assertEquals("out1", operationInfo.getOutputParameterNames().get(0));
		assertEquals("out2", operationInfo.getOutputParameterNames().get(1));
	}

	@After
	public void tearDown() {
		s.kill();
	}
}
