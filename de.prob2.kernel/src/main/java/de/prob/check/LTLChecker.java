package de.prob.check;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.LTLCheckingJob;
import de.prob.animator.domainobjects.LTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.StateSpace;

public class LTLChecker implements IModelCheckJob {

	private final StateSpace s;
	private final IModelCheckListener ui;
	private final String jobId;
	private final LTLCheckingJob job;

	public LTLChecker(final StateSpace s, final String formula)
			throws LtlParseException {
		this(s, s.getModel() instanceof EventBModel ? LTL.parseEventB(formula)
				: new LTL(formula));
	}

	public LTLChecker(final StateSpace s, final LTL formula) {
		this(s, formula, null);
	}

	public LTLChecker(final StateSpace s, final LTL formula,
			final IModelCheckListener ui) {
		if (formula == null) {
			throw new IllegalArgumentException(
					"Cannot perform LTL checking without a correctly parsed LTL Formula");
		}
		this.s = s;
		this.ui = ui;
		this.jobId = ModelChecker.generateJobId();
		job = new LTLCheckingJob(s, formula, jobId, ui);
	}

	@Override
	public IModelCheckingResult call() throws Exception {
		final Stopwatch stopwatch = Stopwatch.createStarted();
		s.execute(job);
		stopwatch.stop();
		IModelCheckingResult result = job.getResult();
		if (ui != null) {
			ui.isFinished(jobId, stopwatch.elapsed(TimeUnit.MILLISECONDS), result,
					null);
		}
		return result;
	}

	@Override
	public IModelCheckingResult getResult() {
		if (job.getResult() == null) {
			return new NotYetFinished("No result was calculated", -1);
		}
		return job.getResult();
	}

	@Override
	public String getJobId() {
		return jobId;
	}

	@Override
	public StateSpace getStateSpace() {
		return s;
	}

}
