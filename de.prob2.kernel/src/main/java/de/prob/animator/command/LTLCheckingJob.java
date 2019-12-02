package de.prob.animator.command;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.prob.animator.domainobjects.LTL;
import de.prob.check.CheckInterrupted;
import de.prob.check.IModelCheckListener;
import de.prob.check.IModelCheckingResult;
import de.prob.check.LTLNotYetFinished;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;

public class LTLCheckingJob extends AbstractCommand {

	private static final int MAX = 500;

	private final StateSpace s;
	private final LTL formula;
	private final String jobId;
	private final IModelCheckListener ui;

	private IModelCheckingResult res;
	private LtlCheckingCommand cmd;
	private final Stopwatch stopwatch;

	public LTLCheckingJob(final StateSpace s, final LTL formula,
			final String jobId, final IModelCheckListener ui) {
		this.s = s;
		this.formula = formula;
		this.jobId = jobId;
		this.ui = ui;
		cmd = new LtlCheckingCommand(s, formula, MAX);
		this.stopwatch = Stopwatch.createUnstarted();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		if (!stopwatch.isRunning()) {
			this.stopwatch.start();
		}
		cmd.writeCommand(pto);
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		cmd.processResult(bindings);
		res = cmd.getResult();
		if (ui != null) {
			ui.updateStats(jobId, this.stopwatch.elapsed(TimeUnit.MILLISECONDS), res, null);
		}
		completed = !(res instanceof LTLNotYetFinished);
		interrupted = interrupted || cmd.isInterrupted();

		cmd = new LtlCheckingCommand(s, formula, MAX);
	}

	public IModelCheckingResult getResult() {
		return res == null && interrupted ? new CheckInterrupted() : res;
	}

	@Override
	public boolean blockAnimator() {
		return true;
	}

}
