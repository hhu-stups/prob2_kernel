package de.prob.check;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.prob.statespace.StateSpace;

/**
 * <p>Internal base class of all checker classes. This class implements almost all parts of the {@link IModelCheckJob} interface and takes care of generating a job ID, measuring execution time, and correctly calling the {@link IModelCheckListener}. Subclasses only need to implement the {@link #execute()} method to perform the actual checking and return the final {@link IModelCheckingResult} object.</p>
 */
abstract class CheckerBase implements IModelCheckJob {
	private final String jobId;
	private final StateSpace stateSpace;
	private final IModelCheckListener listener;
	private final Stopwatch stopwatch;
	private IModelCheckingResult result;
	
	/**
	 * @param stateSpace the {@link StateSpace} in which the checking job is performed
	 * @param listener a listener to notify about checking progress (may be {@code null} if no progress updates are needed)
	 */
	protected CheckerBase(final StateSpace stateSpace, final IModelCheckListener listener) {
		this.jobId = ModelChecker.generateJobId();
		this.stateSpace = stateSpace;
		this.listener = listener;
		this.stopwatch = Stopwatch.createUnstarted();
		this.result = new NotYetFinished("No result was calculated", -1);
	}
	
	@Override
	public String getJobId() {
		return this.jobId;
	}
	
	@Override
	public StateSpace getStateSpace() {
		return this.stateSpace;
	}
	
	/**
	 * <p>Convenience method to call the listener's {@link IModelCheckListener#updateStats(String, long, IModelCheckingResult, StateSpaceStats)} with this checker's job ID and current elapsed time.</p>
	 * 
	 * <p>If this checker has no listener, this method does nothing.</p>
	 * 
	 * @param result passed to the {@code result} parameter of {@link IModelCheckListener#updateStats(String, long, IModelCheckingResult, StateSpaceStats)}
	 * @param stats passed to the {@code stats} parameter of {@link IModelCheckListener#updateStats(String, long, IModelCheckingResult, StateSpaceStats)}
	 */
	protected void updateStats(final IModelCheckingResult result, final StateSpaceStats stats) {
		if (this.listener != null) {
			this.listener.updateStats(this.getJobId(), this.stopwatch.elapsed(TimeUnit.MILLISECONDS), result, stats);
		}
	}
	
	/**
	 * <p>Convenience method to call the listener's {@link IModelCheckListener#isFinished(String, long, IModelCheckingResult, StateSpaceStats)} with this checker's job ID and current elapsed time.</p>
	 * 
	 * <p>If this checker has no listener, this method does nothing.</p>
	 * 
	 * @param result passed to the {@code result} parameter of {@link IModelCheckListener#isFinished(String, long, IModelCheckingResult, StateSpaceStats)}
	 * @param stats passed to the {@code stats} parameter of {@link IModelCheckListener#isFinished(String, long, IModelCheckingResult, StateSpaceStats)}
	 */
	protected void isFinished(final IModelCheckingResult result, final StateSpaceStats stats) {
		if (this.listener != null) {
			this.listener.isFinished(this.getJobId(), this.stopwatch.elapsed(TimeUnit.MILLISECONDS), result, stats);
		}
	}
	
	@Override
	public IModelCheckingResult getResult() {
		return this.result;
	}
	
	/**
	 * 
	 * 
	 * @return the final result of the checking operation
	 */
	protected abstract IModelCheckingResult execute();
	
	@Override
	public IModelCheckingResult call() {
		this.stopwatch.start();
		this.updateStats(new NotYetFinished("Check started", 0), null);
		this.result = this.execute();
		this.stopwatch.stop();
		this.isFinished(this.getResult(), null);
		return this.getResult();
	}
}
