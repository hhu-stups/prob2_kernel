package de.prob.check;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.LTLCheckingJob;
import de.prob.animator.domainobjects.LTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.StateSpace;

public class LTLChecker extends CheckerBase {
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
		super(s, ui);

		if (formula == null) {
			throw new IllegalArgumentException(
					"Cannot perform LTL checking without a correctly parsed LTL Formula");
		}

		job = new LTLCheckingJob(s, formula, this.getJobId(), ui);
	}

	@Override
	protected void execute() {
		this.getStateSpace().execute(job);
		this.isFinished(job.getResult(), null);
	}
}
