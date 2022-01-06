package de.prob.cli.integration.commands;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.animator.command.GetMachineIdentifiersCommand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetMachineIdentifiersCommandTest {

	private Api api;
	private StateSpace s;

	@Before
	public void setupClass() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testGetMachineIdentifiersCommand() throws IOException {
		s = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ExampleMachine.mch").toString());
		assertNotNull(s);
		GetMachineIdentifiersCommand command = new GetMachineIdentifiersCommand(
				GetMachineIdentifiersCommand.Category.VARIABLES);
		s.execute(command);
		assertEquals("x", command.getIdentifiers().get(0));
	}

	@After
	public void tearDown() {
		s.kill();
	}
}
