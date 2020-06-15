package de.prob.cli.integration;

import de.prob.Main;
import de.prob.animator.IAnimator;
import de.prob.animator.ReusableAnimator;
import de.prob.animator.command.GetVersionCommand;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.scripting.ClassicalBFactory;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public final class ReusableAnimatorTest {
	private static ClassicalBFactory modelFactory;
	
	private ReusableAnimator animator;
	
	public ReusableAnimatorTest() {
		super();
	}
	
	@BeforeClass
	public static void beforeClass() {
		modelFactory = Main.getInjector().getInstance(ClassicalBFactory.class);
	}
	
	@Before
	public void setUp() {
		this.animator = Main.getInjector().getInstance(ReusableAnimator.class);
	}
	
	@After
	public void tearDown() {
		this.animator.kill();
		this.animator = null;
	}
	
	private static void checkAnimatorWorking(final IAnimator animator) {
		final GetVersionCommand command = new GetVersionCommand();
		animator.execute(command);
		Assert.assertNotNull(command.getVersionString());
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
			Assert.assertEquals(trace.evalCurrent("x", FormulaExpand.EXPAND).toString(), String.valueOf(i));
			stateSpace.kill();
		}
	}
	
	@Test
	public void testNotMoreThanOneStateSpace() {
		final StateSpace stateSpace = this.animator.createStateSpace();
		checkAnimatorWorking(stateSpace);
		Assert.assertThrows(IllegalStateException.class, this.animator::createStateSpace);
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
		Assert.assertFalse(stateSpace.isKilled());
		Assert.assertFalse(stateSpace.isBusy());
		Assert.assertFalse(this.animator.isBusy());
		stateSpace.startTransaction();
		Assert.assertTrue(stateSpace.isBusy());
		Assert.assertTrue(this.animator.isBusy());
		stateSpace.kill();
		Assert.assertTrue(stateSpace.isKilled());
		Assert.assertFalse(this.animator.isBusy());
	}
}
