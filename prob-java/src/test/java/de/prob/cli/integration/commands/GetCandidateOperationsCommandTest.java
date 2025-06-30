package de.prob.cli.integration.commands;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import de.prob.animator.command.GetCandidateOperationsCommand;
import de.prob.animator.command.GetEnabledOperationsCommand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetCandidateOperationsCommandTest {

	private Api api;
	private StateSpace s;

	@BeforeEach
	public void setupClass() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testGetMachineIdentifiersCommand() throws IOException {
		s = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "MaxOperations.mch").toString());
		assertNotNull(s);

		State state = s.getRoot().perform(Transition.INITIALISE_MACHINE_NAME);
		state.exploreIfNeeded();

		GetEnabledOperationsCommand cmd1 = new GetEnabledOperationsCommand(s, state.getId());
		GetCandidateOperationsCommand cmd2 = new GetCandidateOperationsCommand(state.getId());
		s.execute(cmd1, cmd2);

		assertFalse(state.isTimeoutOccurred());
		assertTrue(state.isMaxTransitionsCalculated());
		assertEquals(Arrays.asList("inc", "inc_many", "set_nondet"), cmd1.getEnabledOperations().stream().map(Transition::getName).sorted().distinct().collect(Collectors.toList()));
		assertEquals(Arrays.asList("dec", "dec_many", "set_nondet2"), cmd2.getCandidates().stream().map(GetCandidateOperationsCommand.Candidate::getOperation).sorted().distinct().collect(Collectors.toList()));
	}

	@AfterEach
	public void tearDown() {
		s.kill();
	}
}
