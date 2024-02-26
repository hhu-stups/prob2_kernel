package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class StateEvaluationTest {
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
	void evalString() {
		Assertions.assertEquals("{PID1}", ((EvalResult)secondState.eval("waiting")).getValue());
	}

	@Test
	void evalIEvalElement() {
		Assertions.assertEquals("{PID1}", ((EvalResult)secondState.eval(new ClassicalB("waiting"))).getValue());
	}

	@Test
	void evalIEvalElementVarargs() {
		List<AbstractEvalResult> results = secondState.eval(new ClassicalB("waiting"), new ClassicalB("ready"));

		Assertions.assertEquals(2, results.size());
		Assertions.assertEquals("{PID1}", ((EvalResult)results.get(0)).getValue());
		String resValue2 = ((EvalResult)results.get(1)).getValue();
		Assertions.assertTrue("{}".equals(resValue2) || "∅".equals(resValue2));
	}

	@Test
	void evalIEvalElementList() {
		List<AbstractEvalResult> results = secondState.eval(Arrays.asList(new ClassicalB("waiting"), new ClassicalB("ready")));

		Assertions.assertEquals(2, results.size());
		Assertions.assertEquals("{PID1}", ((EvalResult)results.get(0)).getValue());
		String resValue2 = ((EvalResult)results.get(1)).getValue();
		Assertions.assertTrue("{}".equals(resValue2) || "∅".equals(resValue2));
	}
}
