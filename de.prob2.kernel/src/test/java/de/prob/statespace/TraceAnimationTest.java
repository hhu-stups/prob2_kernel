package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TraceAnimationTest {
	private static StateSpace s;
	private Trace t;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("groovyTests", "machines", "scheduler.mch").toString();
		ClassicalBFactory factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
		s = factory.extract(path).load();
	}

	@AfterAll
	static void afterAll() {
		s.kill();
	}

	@BeforeEach
	void setUp() {
		t = new Trace(s);
	}

	@Test
	void exploreStateByDefault() {
		try {
			t.setExploreStateByDefault(false);
			// Execute some transitions that aren't covered by any of the other tests
			// (otherwise the destination state could be already automatically explored by another test).
			Trace t2 = t.execute("$initialise_machine")
				.execute("new", "pp = PID3")
				.execute("new", "pp = PID2")
				.execute("new", "pp = PID1")
				.execute("ready", "rr = PID3")
				.execute("ready", "rr = PID2")
				.execute("ready", "rr = PID1");
			Assertions.assertFalse(t2.getCurrentTransition().getDestination().isExplored());
		} finally {
			t.setExploreStateByDefault(true);
		}
	}

	@Test
	void addTransitionByIntegerId() {
		Trace t2 = t.add(0);
		Assertions.assertEquals("$initialise_machine", t2.getCurrentTransition().getName());
	}

	@Test
	void traceSizeEmpty() {
		Assertions.assertEquals(0, t.size());
	}

	@Test
	void traceSizeNonEmpty() {
		Trace t2 = t.anyEvent(null).anyEvent(null).anyEvent(null);
		Assertions.assertEquals(3, t2.size());
	}

	@Test
	void addTransitionByStringId() {
		Trace t2 = t.add("0");
		Assertions.assertEquals("$initialise_machine", t2.getCurrentTransition().getName());
	}

	@Test
	void addTransitionObject() {
		Trace t2 = t.add(t.getCurrentState().getOutTransitions().stream()
			.filter(trans -> "0".equals(trans.getId()))
			.findAny()
			.orElseThrow(AssertionError::new));
		Assertions.assertEquals("$initialise_machine", t2.getCurrentTransition().getName());
	}

	@Test
	void addTransitionByStringIdNotYetExplored() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> t.add("5"));
	}

	@Test
	void addTransitionWith() {
		Trace t2 = t.addTransitionWith("$initialise_machine", Collections.emptyList()).addTransitionWith("new", Collections.singletonList("PID1"));
		Assertions.assertEquals("new", t2.getCurrentTransition().getName());
		Assertions.assertIterableEquals(Collections.singletonList("PID1"), t2.getCurrentTransition().getParameterValues());
	}

	@Test
	void addTransitionWithTypeError() {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			t.addTransitionWith("$initialise_machine", Collections.emptyList()).addTransitionWith("new", Collections.singletonList("PID7"))
		);
	}

	@Test
	void destinationStateExploredByDefault() {
		Trace t2 = t.add("0");
		Assertions.assertTrue(t2.getCurrentTransition().getDestination().isExplored());
	}

	@Test
	void back() {
		Trace t2 = t.execute("$initialise_machine");
		Trace t3 = t2.execute("new", "pp = PID1");
		Trace t4 = t3.back();

		Assertions.assertTrue(t2.canGoBack());
		Assertions.assertEquals(t4.getCurrent(), t2.getCurrent());
		Assertions.assertNotEquals(t4.getCurrent(), t3.getCurrent());
		Assertions.assertEquals(t4.getCurrent(), t3.getCurrent().getPrevious());
	}

	@Test
	void backAtBeginning() {
		Trace t2 = t.back();
		Assertions.assertFalse(t.canGoBack());
		Assertions.assertEquals(t, t2);
	}

	@Test
	void forward() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp = PID1").execute("new", "pp = PID2");
		Trace t3 = t2.back().back();
		Trace t4 = t3.forward();
		Trace t5 = t4.forward();

		Assertions.assertTrue(t3.canGoForward());
		Assertions.assertTrue(t4.canGoForward());
		Assertions.assertFalse(t5.canGoForward());
		Assertions.assertEquals(t2.getCurrent(), t5.getCurrent());
		Assertions.assertEquals(t4.getCurrent(), t5.getCurrent().getPrevious());
		Assertions.assertEquals(t3.getCurrent(), t4.getCurrent().getPrevious());
	}

	@Test
	void forwardAtEnd() {
		Assertions.assertFalse(t.canGoForward());
		Assertions.assertEquals(t, t.forward());
	}

	@Test
	void executeAtEnd() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp=PID1");
		Trace t3 = t2.execute("new", "pp=PID2");

		Assertions.assertEquals(2, t2.getTransitionList().size());
		Assertions.assertEquals(3, t3.getTransitionList().size());
	}

	@Test
	void executeInMiddle() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp=PID1");
		Trace t3 = t2.back().execute("new", "pp=PID2").execute("new", "pp=PID3");

		Assertions.assertEquals(2, t2.getTransitionList().size());
		Assertions.assertEquals(3, t3.getTransitionList().size());
		Assertions.assertNotSame(t3.getTransitionList(), t2.getTransitionList());
	}

	@Test
	void toStringUninitialised() {
		Assertions.assertNotNull(t.toString());
	}

	@Test
	void toStringInitialised() {
		Assertions.assertNotNull(t.execute("$initialise_machine").toString());
	}

	@Test
	void randomAnimationNegative() {
		Assertions.assertEquals(t, t.randomAnimation(-1));
	}

	@Test
	void randomAnimationZero() {
		Assertions.assertEquals(t, t.randomAnimation(0));
	}

	@Test
	void randomAnimationAtEnd() {
		Trace t2 = t.randomAnimation(5);
		Assertions.assertEquals(5, t2.getTransitionList().size());
	}

	@Test
	void randomAnimationInMiddle() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp=PID1");
		Trace t3 = t2.back().randomAnimation(4);

		Assertions.assertNotSame(t3.getTransitionList(), t2.getTransitionList());
		Assertions.assertEquals(5, t3.getTransitionList().size());
	}

	@Test
	void executeList() {
		Trace t2 = t.execute("$initialise_machine").execute("new", Collections.singletonList("pp=PID1"));

		Assertions.assertEquals("new", t2.getCurrentTransition().getName());
		Assertions.assertIterableEquals(Collections.singletonList("PID1"), t2.getCurrentTransition().getParameterValues());
	}

	@Test
	void executeListNonexistant() {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			t.execute("$initialise_machine").execute("new", Collections.singletonList("pp=PID7"))
		);
	}

	@Test
	void executeVarargs() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp=PID1");

		Assertions.assertEquals("new", t2.getCurrentTransition().getName());
		Assertions.assertIterableEquals(Collections.singletonList("PID1"), t2.getCurrentTransition().getParameterValues());
	}

	@Test
	void executeVarargsNonexistant() {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			t.execute("$initialise_machine").execute("new", "pp=PID7")
		);
	}

	@Test
	void canExecuteEventListTrue() {
		Assertions.assertTrue(t.execute("$initialise_machine").canExecuteEvent("new", Collections.singletonList("pp=PID1")));
	}

	@Test
	void canExecuteEventListFalse() {
		Assertions.assertFalse(t.execute("$initialise_machine").canExecuteEvent("new", Collections.singletonList("pp=PID7")));
	}

	@Test
	void canExecuteEventVarargsTrue() {
		Assertions.assertTrue(t.execute("$initialise_machine").canExecuteEvent("new", "pp=PID1"));
	}

	@Test
	void canExecuteEventVarargsFalse() {
		Assertions.assertFalse(t.execute("$initialise_machine").canExecuteEvent("new", "pp=PID7"));
	}

	@Test
	void anyOperation() {
		Trace t2 = t.anyOperation(null);
		Trace t3 = t.anyOperation(null).anyOperation("new");
		// anyOperation currently only recognizes ArrayList and not any kind of List...
		Trace t4 = t.anyOperation(null).anyOperation(new ArrayList<>(Collections.singletonList("new")));
		Trace t5 = t.anyOperation("blah"); // will return original trace

		Assertions.assertEquals("$initialise_machine", t2.getCurrentTransition().getName());
		Assertions.assertEquals("new", t3.getCurrentTransition().getName());
		Assertions.assertEquals("new", t4.getCurrentTransition().getName());
		Assertions.assertEquals(t, t5);
	}

	@Test
	void anyEvent() {
		Trace t2 = t.anyEvent(null);
		Trace t3 = t.anyEvent(null).anyEvent("new");
		// anyEvent currently only recognizes ArrayList and not any kind of List...
		Trace t4 = t.anyEvent(null).anyEvent(new ArrayList<>(Collections.singletonList("new")));
		Trace t5 = t.anyEvent("blah"); // will return original trace

		Assertions.assertEquals("$initialise_machine", t2.getCurrentTransition().getName());
		Assertions.assertEquals("new", t3.getCurrentTransition().getName());
		Assertions.assertEquals("new", t4.getCurrentTransition().getName());
		Assertions.assertEquals(t, t5);
	}

	@Test
	void getTraceFromTransitions() {
		List<Transition> transitions = new ArrayList<>();
		State state = s.getRoot();
		for (int i = 0; i < 10; i++) {
			Transition transition = state.getOutTransitions().get(0);
			transitions.add(transition);
			state = transition.getDestination();
		}
		Trace t1 = Trace.getTraceFromTransitions(s, transitions);
		Trace t2 = Trace.getTraceFromTransitions(s, transitions.subList(3, 6));
		Trace t3 = Trace.getTraceFromTransitions(s, Collections.emptyList()); // empty trace

		Assertions.assertEquals(s.getRoot(), t1.getTransitionList().get(0).getSource());

		Assertions.assertEquals(3, t2.getTransitionList().size());
		Assertions.assertEquals(transitions.get(3), t2.getTransitionList().get(0));
		Assertions.assertEquals(transitions.get(5).getDestination(), t2.getCurrentState());

		Assertions.assertEquals(s.getRoot(), t3.getCurrentState());
	}

	@Test
	void addTransitions() {
		List<Transition> transitions = new ArrayList<>();
		State state = s.getRoot();
		for (int i = 0; i < 10; i++) {
			Transition transition = state.getOutTransitions().get(0);
			transitions.add(transition);
			state = transition.getDestination();
		}
		Trace t1 = t.addTransitions(transitions);
		Trace t2 = t.addTransitions(Collections.emptyList()); // empty

		Assertions.assertEquals(s.getRoot(), t1.getTransitionList().get(0).getSource());
		Assertions.assertEquals(10, t1.getTransitionList().size());

		Assertions.assertEquals(t, t2);
	}
}
