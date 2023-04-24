package de.prob.check;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.LtlCheckingCommand;
import de.prob.animator.domainobjects.LTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.StateSpace;

public class LTLChecker extends CheckerBase {
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
		// Set the state limit to -1 (infinite) for now.
		// Previously, we had set a state limit and called LtlCheckingCommand in a loop until it was done.
		// However, ProB's LTL checker cannot always resume checking after the state limit is reached,
		// so this could result in the same few states getting checked over and over again.
		// TODO Allow LtlCheckingCommand to return some sort of progress information during checking
		final LtlCheckingCommand cmd = new LtlCheckingCommand(this.getStateSpace(), formula, -1);
		this.getStateSpace().withTransaction(() -> this.getStateSpace().execute(cmd));
		this.isFinished(cmd.getResult(), null);
	}
}
