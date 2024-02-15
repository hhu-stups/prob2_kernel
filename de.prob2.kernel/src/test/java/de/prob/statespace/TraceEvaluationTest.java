package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.ComputationNotCompletedResult;
import de.prob.animator.domainobjects.EnumerationWarning;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.animator.domainobjects.WDError;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;
import de.prob.scripting.EventBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TraceEvaluationTest {
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
	void evalCurrentNotInitialised() {
		AbstractEvalResult x = t.evalCurrent("waiting");
		Assertions.assertInstanceOf(IdentifierNotInitialised.class, x);
	}

	@Test
	void evalCurrentEnumerationWarning() {
		AbstractEvalResult x = t.evalCurrent("card({x|x : NATURAL & x mod 2 = 0})");
		Assertions.assertInstanceOf(EnumerationWarning.class, x);
	}

	@Test
	void evalCurrentWDError() {
		AbstractEvalResult x = t.evalCurrent("1 / 0");
		Assertions.assertInstanceOf(WDError.class, x);
	}

	@Test
	void evalCurrentComputationNotCompleted() {
		AbstractEvalResult x = t.evalCurrent("1 + {}");
		Assertions.assertInstanceOf(ComputationNotCompletedResult.class, x);
	}

	@Test
	void evalCurrentEvalResult() {
		Trace t2 = t.execute("$initialise_machine");
		AbstractEvalResult x = t2.evalCurrent("x = waiting & y = card(x)");
		EvalResult result = Assertions.assertInstanceOf(EvalResult.class, x);
		Assertions.assertEquals(result.getValue(), "TRUE");
		Assertions.assertTrue("{}".equals(result.getSolution("x")) || "∅".equals(result.getSolution("x")));
		Assertions.assertEquals("0", result.getSolution("y"));
	}

	@Test
	void evalAllString() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp = PID1").execute("new", "pp = PID2");
		Map<TraceElement, AbstractEvalResult> x = t2.evalAll("waiting");

		Assertions.assertEquals(4, x.size());
		Assertions.assertIterableEquals(t2.getElements(), x.keySet());

		List<AbstractEvalResult> values = new ArrayList<>(x.values());
		Assertions.assertInstanceOf(IdentifierNotInitialised.class, values.get(0));
		String value1 = ((EvalResult)values.get(1)).getValue();
		Assertions.assertTrue("{}".equals(value1) || "∅".equals(value1));
		Assertions.assertEquals("{PID1}", ((EvalResult)values.get(2)).getValue());
		Assertions.assertEquals("{PID1,PID2}", ((EvalResult)values.get(3)).getValue());
	}

	@Test
	void evalAllClassicalB() {
		Trace t2 = t.execute("$initialise_machine").execute("new", "pp = PID1").execute("new", "pp = PID2");
		Map<TraceElement, AbstractEvalResult> x = t2.evalAll(new ClassicalB("waiting"));

		Assertions.assertEquals(4, x.size());
		Assertions.assertIterableEquals(t2.getElements(), x.keySet());

		List<AbstractEvalResult> values = new ArrayList<>(x.values());
		Assertions.assertInstanceOf(IdentifierNotInitialised.class, values.get(0));
		String value1 = ((EvalResult)values.get(1)).getValue();
		Assertions.assertTrue("{}".equals(value1) || "∅".equals(value1));
		Assertions.assertEquals("{PID1}", ((EvalResult)values.get(2)).getValue());
		Assertions.assertEquals("{PID1,PID2}", ((EvalResult)values.get(3)).getValue());
	}

	@Test
	void evalAllEventB() throws IOException {
		String path = Paths.get("groovyTests", "machines", "Lift", "lift0.bcm").toString();
		EventBFactory factory = CliTestCommon.getInjector().getInstance(EventBFactory.class);
		StateSpace s2 = factory.extract(path).load();
		try {
			Trace t2 = new Trace(s2).execute("$setup_constants").execute("$initialise_machine").execute("up");
			Map<TraceElement, AbstractEvalResult> x = t2.evalAll(new EventB("level"));

			Assertions.assertEquals(4, x.size());
			Assertions.assertIterableEquals(t2.getElements(), x.keySet());

			List<AbstractEvalResult> values = new ArrayList<>(x.values());
			Assertions.assertInstanceOf(IdentifierNotInitialised.class, values.get(0));
			Assertions.assertInstanceOf(IdentifierNotInitialised.class, values.get(1));
			Assertions.assertEquals("L0", ((EvalResult)values.get(2)).getValue());
			Assertions.assertEquals("L1", ((EvalResult)values.get(3)).getValue());
		} finally {
			s2.kill();
		}
	}
}
