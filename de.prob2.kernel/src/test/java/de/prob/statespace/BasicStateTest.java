package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.animator.domainobjects.EvalResult;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class BasicStateTest {
	private static StateSpace s;
	private static State root;
	private static State firstState;
	private static State secondState;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("groovyTests", "machines", "scheduler.mch").toString();
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
	void toStringIsId() {
		Assertions.assertEquals("root", root.toString());
	}

	@Test
	void getId() {
		Assertions.assertEquals("root", root.getId());
		Assertions.assertEquals("0", firstState.getId());
	}

	@Test
	void numericalId() {
		Assertions.assertEquals(-1, root.numericalId());
		Assertions.assertEquals(0, firstState.numericalId());
	}

	@Test
	void getStateRep() {
		String rep = secondState.getStateRep();
		Assertions.assertEquals("FALSE", ((EvalResult)firstState.eval(rep)).getValue());
		Assertions.assertEquals("TRUE", ((EvalResult)secondState.eval(rep)).getValue());
	}

	@Test
	void equalsAndHashCode() throws IOException {
		String path = Paths.get("groovyTests", "machines", "scheduler.mch").toString();
		ClassicalBFactory factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
		StateSpace s2 = factory.extract(path).load();
		try {
			State sameroot = new State("root", s);
			State otherroot = new State("root", s2);

			Assertions.assertEquals(root, sameroot);
			Assertions.assertEquals(sameroot, root);
			Assertions.assertEquals(root.hashCode(), sameroot.hashCode());
			Assertions.assertNotEquals(root, otherroot);
			Assertions.assertNotEquals(otherroot, root);
			Assertions.assertNotEquals(root.hashCode(), otherroot.hashCode());
		} finally {
			s2.kill();
		}
	}
}
