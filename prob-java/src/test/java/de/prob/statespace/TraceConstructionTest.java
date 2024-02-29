package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class TraceConstructionTest {
	private static StateSpace s;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "scheduler.mch").toString();
		ClassicalBFactory factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
		s = factory.extract(path).load();
	}

	@AfterAll
	static void afterAll() {
		s.kill();
	}

	@Test
	void getters() {
		Trace t = new Trace(s).execute("$initialise_machine");

		Assertions.assertEquals("0", t.getCurrentState().getId());
		Assertions.assertEquals("root", t.getPreviousState().getId());
		Assertions.assertEquals("$initialise_machine", t.getCurrentTransition().getName());
	}

	@Test
	void getNextTransitions() {
		Trace t = new Trace(s).execute("$initialise_machine");
		Set<Transition> outtrans = t.getNextTransitions();

		Assertions.assertEquals(4, outtrans.size());
		for (Transition trans : outtrans) {
			Assertions.assertFalse(trans.isEvaluated());
		}
	}

	@Test
	void getTransitionList() {
		Trace t = new Trace(s).execute("$initialise_machine").execute("new", "pp=PID1");
		List<Transition> transitions = t.getTransitionList();

		Assertions.assertEquals(2, transitions.size());
		Assertions.assertEquals("$initialise_machine", transitions.get(0).getName());
		Assertions.assertEquals("new", transitions.get(1).getName());
		for (Transition trans : transitions) {
			Assertions.assertFalse(trans.isEvaluated());
		}
	}

	@Test
	void copy() {
		Trace t = new Trace(s).execute("$initialise_machine");
		Trace t2 = t.copy();

		Assertions.assertEquals(t.getCurrent(), t2.getCurrent());
		Assertions.assertEquals(t.getHead(), t2.getHead());
		Assertions.assertEquals(t.getTransitionList(), t2.getTransitionList());
		Assertions.assertNotEquals(t.getUUID(), t2.getUUID());
	}
}
