package de.prob.cli.integration.commands;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.animator.command.GetMachineOperationInfos;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetMachineOperationInfosCommandTest {

	private Api api;
	private StateSpace s;

	@BeforeEach
	public void setupClass() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testGetMachineOperationInfosCommand() throws IOException {
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

	@AfterEach
	public void tearDown() {
		s.kill();
	}
}
