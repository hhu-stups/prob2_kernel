package de.prob.animator.command;

import java.util.Collections;

import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.term.CompoundPrologTerm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class SpecializedBooleanCheckersCommandTest {
	@Test
	void checkInitialisationStatusCommand() {
		CheckInitialisationStatusCommand command = new CheckInitialisationStatusCommand("root");

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("true"))));
		Assertions.assertTrue(command.isInitialized());

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("false"))));
		Assertions.assertFalse(command.isInitialized());
	}

	@Test
	void checkInvariantStatusCommand() {
		CheckInvariantStatusCommand command = new CheckInvariantStatusCommand("root");

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("true"))));
		Assertions.assertTrue(command.isInvariantViolated());

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("false"))));
		Assertions.assertFalse(command.isInvariantViolated());
	}

	@Test
	void checkMaxOperationReachedStatusCommand() {
		CheckMaxOperationReachedStatusCommand command = new CheckMaxOperationReachedStatusCommand("root");

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("true"))));
		Assertions.assertTrue(command.maxOperationReached());

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("false"))));
		Assertions.assertFalse(command.maxOperationReached());
	}

	@Test
	void checkTimeoutStatusCommand() {
		CheckTimeoutStatusCommand command = new CheckTimeoutStatusCommand("root");

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("true"))));
		Assertions.assertTrue(command.isTimeout());

		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("false"))));
		Assertions.assertFalse(command.isTimeout());
	}
}
