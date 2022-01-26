package de.prob.cli.integration;

import de.prob.animator.IAnimator;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.GetVersionCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.ClassicalBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class ReusableAnimatorTest {
	private static ClassicalBFactory modelFactory;
	
	private ReusableAnimator animator;
	
	public ReusableAnimatorTest() {
		super();
	}
	
	@BeforeAll
	public static void beforeClass() {
		modelFactory = CliTestCommon.getInjector().getInstance(ClassicalBFactory.class);
	}
	
	@BeforeEach
	public void setUp() {
		this.animator = CliTestCommon.getInjector().getInstance(ReusableAnimator.class);
	}
	
	@AfterEach
	public void tearDown() {
		this.animator.kill();
		this.animator = null;
	}
	
	private static void checkAnimatorWorking(final IAnimator animator) {
		final GetVersionCommand command = new GetVersionCommand();
		animator.execute(command);
		Assertions.assertNotNull(command.getVersionString());
	}
	
	@Test
	public void testDirectAnimatorUse() {
		checkAnimatorWorking(this.animator);
	}
	
	@Test
	public void testLoadMultipleModels() {
		for (int i = 0; i < 4; i++) {
			final StateSpace stateSpace = this.animator.createStateSpace();
			checkAnimatorWorking(stateSpace);
			modelFactory.create("Test" + i, String.format("MACHINE Test%d VARIABLES x INVARIANT x : INTEGER INITIALISATION x := %d END", i, i))
				.loadIntoStateSpace(stateSpace);
			final Trace trace = new Trace(stateSpace).randomAnimation(1);
			Assertions.assertEquals(trace.evalCurrent("x", FormulaExpand.EXPAND).toString(), String.valueOf(i));
			stateSpace.kill();
		}
	}
	
	@Test
	public void testNotMoreThanOneStateSpace() {
		final StateSpace stateSpace = this.animator.createStateSpace();
		checkAnimatorWorking(stateSpace);
		Assertions.assertThrows(IllegalStateException.class, this.animator::createStateSpace);
		checkAnimatorWorking(stateSpace);
		stateSpace.kill();
		final StateSpace newStateSpace = this.animator.createStateSpace();
		checkAnimatorWorking(newStateSpace);
		newStateSpace.kill();
	}
	
	@Test
	public void testKillBusy() {
		final StateSpace stateSpace = this.animator.createStateSpace();
		checkAnimatorWorking(stateSpace);
		Assertions.assertFalse(stateSpace.isKilled());
		Assertions.assertFalse(stateSpace.isBusy());
		Assertions.assertFalse(this.animator.isBusy());
		stateSpace.startTransaction();
		Assertions.assertTrue(stateSpace.isBusy());
		Assertions.assertTrue(this.animator.isBusy());
		stateSpace.kill();
		Assertions.assertTrue(stateSpace.isKilled());
		Assertions.assertFalse(this.animator.isBusy());
	}
}
