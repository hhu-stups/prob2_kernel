package de.prob.check;

import com.google.common.base.Stopwatch;

import de.prob.animator.CommandInterruptedException;
import de.prob.statespace.StateSpace;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Internal base class of all checker classes. This class implements almost all parts of the {@link IModelCheckJob} interface and takes care of generating a job ID, measuring execution time, and correctly calling the {@link IModelCheckListener}. Subclasses only need to implement the {@link #execute()} method to perform the actual checking and return the final {@link IModelCheckingResult} object.</p>
 */
abstract class CheckerBase implements IModelCheckJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckerBase.class);
	private static final String JOB_ID_PREFIX = "mc";
	private static final NotYetFinished UNSET_RESULT = new NotYetFinished("No result was calculated", -1);
	private static final AtomicLong jobIdCounter = new AtomicLong();
	
	private final String jobId;
	private final StateSpace stateSpace;
	private final IModelCheckListener listener;
	protected final Stopwatch stopwatch;
	private IModelCheckingResult result;
	
	/**
	 * @param stateSpace the {@link StateSpace} in which the checking job is performed
	 * @param listener a listener to notify about checking progress (may be {@code null} if no progress updates are needed)
	 */
	protected CheckerBase(final StateSpace stateSpace, final IModelCheckListener listener) {
		this.jobId = generateJobId();
		this.stateSpace = stateSpace;
		this.listener = listener;
		this.stopwatch = Stopwatch.createUnstarted();
		this.result = UNSET_RESULT;
	}
	
	protected static String generateJobId() {
		return JOB_ID_PREFIX + jobIdCounter.getAndIncrement();
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
	 * <p>This method is also used to set the check result, so it must be called even if no listener is present.</p>
	 * 
	 * @param result passed to the {@code result} parameter of {@link IModelCheckListener#isFinished(String, long, IModelCheckingResult, StateSpaceStats)}
	 * @param stats passed to the {@code stats} parameter of {@link IModelCheckListener#isFinished(String, long, IModelCheckingResult, StateSpaceStats)}
	 */
	protected void isFinished(final IModelCheckingResult result, final StateSpaceStats stats) {
		if (this.result != UNSET_RESULT) {
			throw new IllegalStateException(CheckerBase.class + ".isFinished must not be called more than once");
		}
		
		this.result = result;
		
		if (this.listener != null) {
			this.listener.isFinished(this.getJobId(), this.stopwatch.elapsed(TimeUnit.MILLISECONDS), result, stats);
		}
	}
	
	@Override
	public IModelCheckingResult getResult() {
		return this.result;
	}
	
	protected abstract void execute();
	
	@Override
	public IModelCheckingResult call() {
		this.stopwatch.start();
		this.updateStats(new NotYetFinished("Check started", 0), null);
		try {
			this.execute();
		} catch (CommandInterruptedException exc) {
			// Provide sensible default handling for interrupts.
			// Implementations can also handle CommandInterruptedException themselves in .execute
			// to return stats or a different result.
			LOGGER.info("{} received a Prolog interrupt", this.getClass().getSimpleName(), exc);
			this.isFinished(new CheckInterrupted(), null);
		}
		this.stopwatch.stop();
		if (this.getResult() == UNSET_RESULT) {
			throw new IllegalStateException(CheckerBase.class.getSimpleName() + ".execute implementations must call isFinished before returning");
		}
		return this.getResult();
	}
}
