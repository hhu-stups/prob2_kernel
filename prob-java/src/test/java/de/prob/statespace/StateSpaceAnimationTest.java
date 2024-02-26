package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class StateSpaceAnimationTest {
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
	void getStateStringInvalid() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> s.getState("bum!"));
	}

	@Test
	void getStateIntegerRoot() {
		Assertions.assertEquals(root, s.getState(-1));
	}

	@Test
	void getStateInteger() {
		root.explore();
		Assertions.assertEquals(s.getState("0"), s.getState(0));
	}

	@Test
	void getStateIntegerInvalid() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> s.getState(-100));
	}

	@Test
	void getStateIntegerNotYetExplored() {
		// we have not reached this during the exploration we have done so far in the tests
		Assertions.assertThrows(IllegalArgumentException.class, () -> s.getState(500));
	}

	@Test
	void getStatesFromPredicate() {
		firstState.perform("new", "pp=PID1").perform("new", "pp=PID2");
		IEvalElement formula = new ClassicalB("card(waiting) > 0");
		for (State state : s.getStatesFromPredicate(formula)) {
			AbstractEvalResult evaluated = state.eval(formula);
			Assertions.assertTrue(evaluated instanceof IdentifierNotInitialised || "TRUE".equals(((EvalResult)evaluated).getValue()));
		}
	}

	@Test
	void transitionFromPredicateSingle() {
		List<Transition> transitions = s.transitionFromPredicate(firstState, "new", "pp=PID1", 1);
		Assertions.assertEquals(1, transitions.size());
		Assertions.assertEquals("new", transitions.get(0).getName());
		Assertions.assertEquals(1, transitions.get(0).getParameterValues().size());
		Assertions.assertEquals("PID1", transitions.get(0).getParameterValues().get(0));
	}

	@Test
	void transitionFromPredicateMultiple() {
		List<Transition> transitions = s.transitionFromPredicate(firstState, "new", "TRUE=TRUE", 3);
		Assertions.assertEquals(3, transitions.size());

		Set<String> parameterValues = new HashSet<>();
		for (Transition transition : transitions) {
			Assertions.assertEquals("new", transition.getName());
			Assertions.assertEquals(1, transition.getParameterValues().size());
			parameterValues.add(transition.getParameterValues().get(0));
		}
		Assertions.assertEquals(new HashSet<>(Arrays.asList("PID1", "PID2", "PID3")), parameterValues);
	}

	@Test
	void transitionFromPredicateZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> s.transitionFromPredicate(firstState, "new", "TRUE=TRUE", 0));
	}

	@Test
	void transitionFromPredicateTypeError() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> s.transitionFromPredicate(firstState, "new", "TRUE=true", 1));
	}

	@Test
	void isValidOperationValid() {
		Assertions.assertTrue(s.isValidOperation(firstState, "new", "pp=PID1"));
	}

	@Test
	void isValidOperationTypeError() {
		Assertions.assertFalse(s.isValidOperation(firstState, "new", "TRUE=true"));
	}

	@Test
	void isValidOperationNoSolutions() {
		Assertions.assertFalse(s.isValidOperation(firstState, "new", "TRUE=FALSE"));
	}

	@Test
	void getTraceToStateId() {
		State state = firstState.perform("new", "pp=PID1").perform("new", "pp=PID2").perform("new", "pp=PID3");
		Trace t = s.getTrace(state.getId());
		Assertions.assertEquals(state, t.getCurrentState());
	}

	@Test
	void getTraceBetweenStates() {
		State state = firstState.perform("new", "pp=PID1").perform("new", "pp=PID2").perform("new", "pp=PID3");
		Trace t = s.getTrace(firstState.getId(), state.getId());
		Assertions.assertEquals(firstState, t.getTransitionList().get(0).getSource());
		Assertions.assertEquals(state, t.getCurrentState());
	}

	@Test
	void getTraceFromTransitionIds() {
		List<Transition> transitions = new ArrayList<>();
		Transition tr = root.findTransition("$initialise_machine");
		transitions.add(tr);
		tr = tr.getDestination().findTransition("new", "pp=PID1");
		transitions.add(tr);
		tr = tr.getDestination().findTransition("new", "pp=PID2");
		Trace t = s.getTrace(transitions.stream().map(Transition::getId).collect(Collectors.toList()));
		Assertions.assertIterableEquals(transitions, t.getTransitionList());
	}

	@Test
	void getTraceToStatePredicate() {
		IEvalElement formula = new ClassicalB("waiting = {PID1,PID3}");
		Trace t = s.getTraceToState(formula);
		Assertions.assertEquals("TRUE", ((EvalResult)t.evalCurrent(formula)).getValue());
	}
}
