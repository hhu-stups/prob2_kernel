package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.animator.domainobjects.EvalResult;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class StateAnimationTest {
	private static StateSpace s;
	private static State root;
	private static State firstState;
	private static State secondState;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "scheduler.mch").toString();
		ClassicalBFactory factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
		s = factory.extract(path).load();
		root = s.getRoot();
		firstState = root.perform("$initialise_machine");
		secondState = firstState.perform("new", "pp=PID1");
	}

	@AfterAll
	static void afterAll() {
		s.kill();
	}

	@Test
	void performList() {
		Assertions.assertEquals(firstState, root.perform("$initialise_machine", Collections.emptyList()));
		Assertions.assertEquals(secondState, firstState.perform("new", Collections.singletonList("pp=PID1")));
	}

	@Test
	void performNonexistant() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> root.perform("blah"));
	}

	@Test
	void findTransitionVarargs() {
		Assertions.assertEquals("$initialise_machine", root.findTransition("$initialise_machine").getName());
		Assertions.assertEquals("new", firstState.findTransition("new", "pp=PID1").getName());
	}

	@Test
	void findTransitionList() {
		Assertions.assertEquals("$initialise_machine", root.findTransition("$initialise_machine", Collections.emptyList()).getName());
		Assertions.assertEquals("new", firstState.findTransition("new", Collections.singletonList("pp=PID1")).getName());
	}

	@Test
	void findTransitionNonexistant() {
		Assertions.assertNull(root.findTransition("blah"));
	}

	@Test
	void findTransitionUsesCachedTransition() {
		// This isn't a very nice test - it temporarily modifies the state's internal list of transitions...
		// If this breaks at some point in the future, feel free to just remove the entire test.
		Transition t = Transition.generateArtificialTransition(s, "blah", "blah", "blah", "blah");
		root.getTransitions().add(t);

		try {
			Assertions.assertEquals(t, root.findTransition("blah"));
		} finally {
			root.getTransitions().remove(t);
		}
	}

	@Test
	void findTransitionsWithOrWithoutPredicate() {
		List<Transition> transitions1 = firstState.findTransitions("new", Collections.emptyList(), 3);
		Assertions.assertEquals(3, transitions1.size());

		List<Transition> transitions2 = firstState.findTransitions("new", Collections.singletonList("pp=PID1"), 1);
		Assertions.assertEquals(1, transitions2.size());
		Assertions.assertEquals(1, transitions2.get(0).getParameterValues().size());
		Assertions.assertEquals("PID1", transitions2.get(0).getParameterValues().get(0));
	}

	@Test
	void anyOperation() {
		State s2 = root.anyOperation(null);
		State s3 = root.anyOperation(null).anyOperation("new");
		// anyOperation currently only recognizes ArrayList and not any kind of List...
		State s4 = root.anyOperation(null).anyOperation(new ArrayList<>(Collections.singletonList("new")));
		State s5 = root.anyOperation("blah"); // will return original state

		Assertions.assertEquals(firstState, s2);
		Assertions.assertNotEquals("{}", ((EvalResult)s3.eval("waiting")).getValue());
		Assertions.assertNotEquals("{}", ((EvalResult)s4.eval("waiting")).getValue());
		Assertions.assertEquals(root, s5);
	}

	@Test
	void anyEvent() {
		State s2 = root.anyEvent(null);
		State s3 = root.anyEvent(null).anyEvent("new");
		// anyEvent currently only recognizes ArrayList and not any kind of List...
		State s4 = root.anyEvent(null).anyEvent(new ArrayList<>(Collections.singletonList("new")));
		State s5 = root.anyEvent("blah"); // will return original state

		Assertions.assertEquals(firstState, s2);
		Assertions.assertNotEquals("{}", ((EvalResult)s3.eval("waiting")).getValue());
		Assertions.assertNotEquals("{}", ((EvalResult)s4.eval("waiting")).getValue());
		Assertions.assertEquals(root, s5);
	}
}
