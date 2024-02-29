package de.prob.statespace;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AnimationSelectorTest {
	static final class TestListener implements IAnimationChangeListener {
		Trace lastNotifiedFor = null;

		@Override
		public void traceChange(Trace currentTrace, boolean currentAnimationChanged) {
			this.lastNotifiedFor = currentTrace;
		}

		@Override
		public void animatorStatus(boolean busy) {}
	}

	private static StateSpace s;
	private static AnimationSelector selector;
	private static AnimationSelectorTest.TestListener listener;
	private Trace t;

	@BeforeAll
	static void beforeAll() throws IOException {
		String path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "scheduler.mch").toString();
		ClassicalBFactory factory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
		s = factory.extract(path).load();
		selector = CliTestCommon.getInjector().getInstance(AnimationSelector.class);
		listener = new AnimationSelectorTest.TestListener();
		selector.registerAnimationChangeListener(listener);
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
	void addNewAnimation() {
		selector.addNewAnimation(t);
		Assertions.assertEquals(t, listener.lastNotifiedFor);
		Assertions.assertTrue(selector.getTraces().contains(t));
	}

	@Test
	void changeCurrentAnimation() {
		selector.changeCurrentAnimation(t);
		Assertions.assertEquals(t, listener.lastNotifiedFor);
		Assertions.assertEquals(t, selector.getCurrentTrace());
	}
}
