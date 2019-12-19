package de.prob.check;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.LtlCheckingCommand;
import de.prob.animator.domainobjects.LTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.StateSpace;

public class LTLChecker extends CheckerBase {
	private static final int MAX = 500;

	private final LTL formula;

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

		this.formula = formula;
	}

	@Override
	protected void execute() {
		final LtlCheckingCommand cmd = new LtlCheckingCommand(this.getStateSpace(), formula, MAX);
		try {
			this.getStateSpace().startTransaction();
			do {
				this.getStateSpace().execute(cmd);
				this.updateStats(cmd.getResult(), null);
			} while (cmd.getResult() instanceof LTLNotYetFinished);
		} finally {
			this.getStateSpace().endTransaction();
		}
		this.isFinished(cmd.getResult(), null);
	}
}
