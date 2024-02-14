package de.prob.statespace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.CSP;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.cli.CliTestCommon;
import de.prob.model.representation.CSPModel;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class StateSpaceEvaluationTest {
	private static final class DummyObject {
		private final String field = "I don't do much!";
	}

	private static StateSpace s;
	private static State root;
	private static State firstState;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("groovyTests", "machines", "scheduler.mch").toString();
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
	void isInitialisedRoot() {
		Assertions.assertFalse(root.isInitialised());
	}

	@Test
	void eval() {
		List<AbstractEvalResult> res = firstState.eval(Arrays.asList(new ClassicalB("waiting"), new ClassicalB("ready")));
		Assertions.assertEquals(2, res.size());
		String resValue1 = ((EvalResult)res.get(0)).getValue();
		Assertions.assertTrue("{}".equals(resValue1) || "∅".equals(resValue1));
		String resValue2 = ((EvalResult)res.get(1)).getValue();
		Assertions.assertTrue("{}".equals(resValue2) || "∅".equals(resValue2));
	}

	@Test
	void subscribeSingleFormulaSingleSubscriber() {
		IEvalElement formula = new ClassicalB("waiting /\\ ready");
		Assertions.assertFalse(s.isSubscribed(formula));
		String subscriber = "I am a subscriber!";
		Assertions.assertTrue(s.subscribe(subscriber, formula));
		Assertions.assertTrue(s.isSubscribed(formula));
	}

	@Test
	void subscribeSingleFormulaMultipleSubscribers() {
		IEvalElement formula = new ClassicalB("waiting \\/ ready");
		Assertions.assertFalse(s.isSubscribed(formula));
		String subscriber1 = "I am a subscriber!";
		String subscriber2 = "I am also a subscriber!";
		Assertions.assertTrue(s.subscribe(subscriber1, formula));
		Assertions.assertTrue(s.subscribe(subscriber2, formula));
		Assertions.assertTrue(s.isSubscribed(formula));
	}

	@Test
	void subscribeSingleCspFormula() {
		CSPModel m = CliTestCommon.getInjector().getInstance(CSPModel.class);
		m = m.create("some content", Paths.get("somedir", "someotherdir", "myfile.csp").toFile());
		IEvalElement csp = new CSP("some formula", m);
		String subscriber1 = "subscriber1";
		// CSP formulas cannot be subscribed
		Assertions.assertFalse(s.subscribe(subscriber1, csp));
		Assertions.assertFalse(s.isSubscribed(csp));
	}

	@Test
	void subscribeMultipleFormulasSingleSubscriber() {
		IEvalElement formula = new ClassicalB("card(waiting)");
		IEvalElement formula2 = new ClassicalB("card(ready)");
		Assertions.assertFalse(s.isSubscribed(formula));
		Assertions.assertFalse(s.isSubscribed(formula2));
		String subscriber = "I am a subscriber!";
		Assertions.assertTrue(s.subscribe(subscriber, Arrays.asList(formula, formula2)));
		Assertions.assertTrue(s.isSubscribed(formula));
		Assertions.assertTrue(s.isSubscribed(formula2));
	}

	@Test
	void subscribeMultipleFormulasMultipleSubscribers() {
		IEvalElement formula = new ClassicalB("card(ready)+card(waiting)");
		IEvalElement formula2 = new ClassicalB("card(active)");
		Assertions.assertFalse(s.isSubscribed(formula));
		Assertions.assertFalse(s.isSubscribed(formula2));
		String subscriber1 = "I am a subscriber!";
		String subscriber2 = "I am also a subscriber!";
		Assertions.assertTrue(s.subscribe(subscriber1, Arrays.asList(formula, formula2)));
		Assertions.assertTrue(s.subscribe(subscriber2, Arrays.asList(formula, formula2)));
		Assertions.assertTrue(s.isSubscribed(formula));
		Assertions.assertTrue(s.isSubscribed(formula2));
	}

	@Test
	void subscribeMultipleCspFormulas() {
		CSPModel m = CliTestCommon.getInjector().getInstance(CSPModel.class);
		m = m.create("some content", Paths.get("somedir", "someotherdir", "myfile.csp").toFile());
		IEvalElement csp = new CSP("some formula", m);
		IEvalElement csp2 = new CSP("some formula2", m);
		String subscriber1 = "subscriber1";
		// CSP formulas cannot be subscribed
		Assertions.assertFalse(s.subscribe(subscriber1, Arrays.asList(csp, csp2)));
		Assertions.assertFalse(s.isSubscribed(csp));
		Assertions.assertFalse(s.isSubscribed(csp2));
	}

	@Test
	void getValues() {
		IEvalElement formula = new ClassicalB("card(waiting) + 1");
		Assertions.assertTrue(s.subscribe("mmm", formula));
		firstState.explore();
		Map<IEvalElement, AbstractEvalResult> values = firstState.getValues();
		Assertions.assertTrue(values.containsKey(formula));
		Assertions.assertEquals("1", ((EvalResult)values.get(formula)).getValue());
	}

	@Test
	void isSubscribed() {
		Assertions.assertFalse(s.isSubscribed(new ClassicalB("card(waiting)+10")));
	}

	@Disabled("This doesn't reliably trigger a garbage collection. System.gc() was sufficient under Groovy/Spock, but for some reason not under Java/JUnit.")
	@Test
	void unsubscribeAutomaticallyByGarbageCollection() {
		IEvalElement formula = new ClassicalB("card(ready) + 1");
		Object subscriber = new StateSpaceEvaluationTest.DummyObject();
		Assertions.assertTrue(s.subscribe(subscriber, formula));
		Assertions.assertTrue(s.isSubscribed(formula));
		subscriber = null;
		System.gc();
		Assertions.assertFalse(s.isSubscribed(formula));
	}

	@Test
	void unsubscribeSingleSubscriber() {
		String subscriber = "I'm a subscriber!";
		IEvalElement formula = new ClassicalB("card(waiting) + 5");
		Assertions.assertTrue(s.subscribe(subscriber, formula));
		Assertions.assertTrue(s.isSubscribed(formula));
		Assertions.assertTrue(s.unsubscribe(subscriber, formula));
		Assertions.assertFalse(s.isSubscribed(formula));
	}

	@Test
	void unsubscribeMultipleSubscribers() {
		String subscriber = "hi!";
		String subscriber2 = "hi again!";
		IEvalElement formula = new ClassicalB("card(ready) + card(active) + 7");
		Assertions.assertTrue(s.subscribe(subscriber, formula));
		Assertions.assertTrue(s.subscribe(subscriber2, formula));
		Assertions.assertTrue(s.unsubscribe(subscriber, formula));
		Assertions.assertTrue(s.isSubscribed(formula));
	}

	@Test
	void unsubscribeWhenNotSubscribed() {
		Assertions.assertFalse(s.unsubscribe("I'm not a subscriber", new ClassicalB("1+24")));
	}

	@Test
	void getSubscribedFormulas() {
		String subscriber = "hi!";
		IEvalElement formula = new ClassicalB("card(active) + 9");
		Assertions.assertTrue(s.subscribe(subscriber, formula));
		Assertions.assertTrue(s.getSubscribedFormulas().contains(formula));
		Assertions.assertTrue(s.unsubscribe(subscriber, formula));
		Assertions.assertFalse(s.getSubscribedFormulas().contains(formula));
	}

	@Test
	void evaluateForGivenStates() {
		IEvalElement waiting = new ClassicalB("waiting");
		IEvalElement ready = new ClassicalB("ready");
		IEvalElement active = new ClassicalB("active");
		State state2 = firstState.perform("new", "pp=PID1");
		State state3 = firstState.perform("new", "pp=PID2");
		State state4 = firstState.perform("new", "pp=PID3");
		List<State> states = Arrays.asList(root, firstState, state2, state3, state4);
		Assertions.assertTrue(s.subscribe("I'm a subscriber!", Collections.singletonList(ready)));
		Map<State, Map<IEvalElement, AbstractEvalResult>> result = s.evaluateForGivenStates(states, Arrays.asList(waiting, ready, active));

		for (AbstractEvalResult res : result.get(root).values()) {
			Assertions.assertInstanceOf(IdentifierNotInitialised.class, res);
		}

		for (State state : states) {
			if (state == root) {
				continue;
			}
			Map<IEvalElement, AbstractEvalResult> resultForState = result.get(state);
			String readyValue = ((EvalResult)resultForState.get(ready)).getValue();
			Assertions.assertTrue("{}".equals(readyValue) || "∅".equals(readyValue));
			String activeValue = ((EvalResult)resultForState.get(active)).getValue();
			Assertions.assertTrue("{}".equals(activeValue) || "∅".equals(activeValue));
		}

		String waitingRootValue = ((EvalResult)result.get(firstState).get(waiting)).getValue();
		Assertions.assertTrue("{}".equals(waitingRootValue) || "∅".equals(waitingRootValue));
		Assertions.assertEquals("{PID1}", ((EvalResult)result.get(state2).get(waiting)).getValue());
		Assertions.assertEquals("{PID2}", ((EvalResult)result.get(state3).get(waiting)).getValue());
		Assertions.assertEquals("{PID3}", ((EvalResult)result.get(state4).get(waiting)).getValue());
	}
}
