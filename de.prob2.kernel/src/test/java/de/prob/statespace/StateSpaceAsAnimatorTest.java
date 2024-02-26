package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

import de.prob.animator.command.CheckInitialisationStatusCommand;
import de.prob.animator.command.CheckInvariantStatusCommand;
import de.prob.animator.command.ExploreStateCommand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class StateSpaceAsAnimatorTest {
	private static StateSpace s;
	private static State root;
	private static State firstState;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "scheduler.mch").toString();
		ClassicalBFactory factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
		s = factory.extract(path).load();
		root = s.getRoot();
		firstState = root.perform("$initialise_machine");
	}

	@AfterAll
	static void afterAll() {
		s.kill();
	}

	@Test
	void sendInterruptDoesNotAffectLaterCommands() {
		s.sendInterrupt();
		s.execute(new ExploreStateCommand(s, "root", Collections.emptyList()));
	}

	@Test
	void executeSingleCommand() {
		CheckInitialisationStatusCommand cmd = new CheckInitialisationStatusCommand(root.getId());
		s.execute(cmd);
		Assertions.assertFalse(cmd.getResult());
	}

	@Test
	void executeMultipleCommands() {
		CheckInitialisationStatusCommand cmd = new CheckInitialisationStatusCommand(firstState.getId());
		CheckInvariantStatusCommand cmd2 = new CheckInvariantStatusCommand(firstState.getId());
		s.execute(cmd, cmd2);
		Assertions.assertTrue(cmd.getResult());
		Assertions.assertFalse(cmd2.isInvariantViolated());
	}

	@Test
	void transaction() {
		Assertions.assertFalse(s.isBusy());
		s.startTransaction();
		Assertions.assertTrue(s.isBusy());
		s.endTransaction();
		Assertions.assertFalse(s.isBusy());
		s.withTransaction(() -> Assertions.assertTrue(s.isBusy()));
		Assertions.assertFalse(s.isBusy());
	}

	@Test
	void transactionNotifiesAnimationChangeListener() {
		AnimationSelector animations = CliTestCommon.getInjector().getInstance(AnimationSelector.class);
		animations.addNewAnimation(new Trace(s));

		boolean[] becameBusy = {false};
		boolean[] becameNotBusyAgain = {false};
		animations.registerAnimationChangeListener(new IAnimationChangeListener() {
			@Override
			public void traceChange(Trace currentTrace, boolean currentAnimationChanged) {}
			
			@Override
			public void animatorStatus(boolean busy) {
				if (busy) {
					becameBusy[0] = true;
				} else if (becameBusy[0]) {
					becameNotBusyAgain[0] = true;
				}
			}
		});

		s.startTransaction();
		s.endTransaction();

		Assertions.assertTrue(becameBusy[0]);
		Assertions.assertTrue(becameNotBusyAgain[0]);
	}
}
