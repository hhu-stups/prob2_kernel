package de.prob.animator;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.annotations.MaxCacheSize;
import de.prob.statespace.StateSpace;

/**
 * <p>
 * An animator that can be used by more than one model/state space.
 * Internally, the animator reuses the same instance of {@code probcli} for all state spaces,
 * which allows loading new machines more quickly than by starting a new {@code probcli} instance for every model.
 * </p>
 * <p>
 * A {@link ReusableAnimator} instance can only be used by one model/state space at a time.
 * To load a new model into the animator,
 * the old model/state space must be killed first.
 * </p>
 * <p>
 * By default, some state is maintained across models in a single reusable animator.
 * Notably, preferences are not automatically reset,
 * so preferences changed in the context of a previous model will remain changed for future models.
 * This behavior is desirable in some cases,
 * e. g. in a REPL environment like the Jupyter kernel,
 * but not in other cases like the ProB 2 UI.
 * To fully reset the animator to a clean state as if it was newly started,
 * call {@link #resetProB()} before creating a new state space.
 * </p>
 */
public final class ReusableAnimator implements IAnimator {
	private final IAnimator animator;
	private final int maxCacheSize;
	
	private final Object currentStateSpaceLock;
	private StateSpace currentStateSpace;
	
	@Inject
	private ReusableAnimator(final IAnimator animator, @MaxCacheSize final int maxCacheSize) {
		super();
		
		this.animator = animator;
		this.maxCacheSize = maxCacheSize;
		
		this.currentStateSpaceLock = new Object();
		this.currentStateSpace = null;
	}
	
	/**
	 * Get the state space that is currently using (or last used) this animator, or {@code null} if the animator has not yet been used.
	 * The returned {@link StateSpace} might already be killed and no longer actually using this animator.
	 * 
	 * @return the state space that is currently using (or last used) this animator, or {@code null} if the animator has not yet been used
	 */
	public StateSpace getCurrentStateSpace() {
		return this.currentStateSpace;
	}
	
	/**
	 * Create a new state space that uses this animator.
	 * This method cannot be called again until the state space returned from the last call is killed.
	 * Killing a state space returned from this method does not actually kill the animator,
	 * it only disables that state space and allows a new one to be created.
	 * 
	 * @return a new state space that uses this animator
	 */
	public StateSpace createStateSpace() {
		synchronized (this.currentStateSpaceLock) {
			if (this.getCurrentStateSpace() != null && !this.getCurrentStateSpace().isKilled()) {
				throw new IllegalStateException("The animator is already in use");
			}
			
			this.currentStateSpace = new StateSpace(this, false, this.maxCacheSize);
			return this.currentStateSpace;
		}
	}
	
	@Override
	public void execute(final AbstractCommand command) {
		this.animator.execute(command);
	}
	
	@Override
	public void sendInterrupt() {
		this.animator.sendInterrupt();
	}
	
	@Override
	public void kill() {
		this.animator.kill();
	}
	
	@Override
	public void startTransaction() {
		this.animator.startTransaction();
	}
	
	@Override
	public void endTransaction() {
		this.animator.endTransaction();
	}
	
	@Override
	public boolean isBusy() {
		return this.animator.isBusy();
	}
	
	@Override
	public String getId() {
		return this.animator.getId();
	}
	
	@Override
	public void resetProB() {
		synchronized (this.currentStateSpaceLock) {
			if (this.getCurrentStateSpace() != null && !this.getCurrentStateSpace().isKilled()) {
				throw new IllegalStateException("Cannot reset ReusableAnimator's ProB instance while it is still in use - kill the StateSpace first");
			}
			
			this.animator.resetProB();
		}
	}
	
	@Override
	public long getTotalNumberOfErrors() {
		return this.animator.getTotalNumberOfErrors();
	}
	
	@Override
	public void addBusyListener(IAnimatorBusyListener listener) {
		this.animator.addBusyListener(listener);
	}
	
	@Override
	public void removeBusyListener(IAnimatorBusyListener listener) {
		this.animator.removeBusyListener(listener);
	}
	
	@Override
	public void addWarningListener(final IWarningListener listener) {
		this.animator.addWarningListener(listener);
	}
	
	@Override
	public void removeWarningListener(final IWarningListener listener) {
		this.animator.removeWarningListener(listener);
	}
	
	@Override
	public void addConsoleOutputListener(final IConsoleOutputListener listener) {
		this.animator.addConsoleOutputListener(listener);
	}
	
	@Override
	public void removeConsoleOutputListener(final IConsoleOutputListener listener) {
		this.animator.removeConsoleOutputListener(listener);
	}
}
