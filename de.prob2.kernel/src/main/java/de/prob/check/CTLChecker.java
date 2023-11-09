package de.prob.check;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.CtlCheckingCommand;
import de.prob.animator.domainobjects.CTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.StateSpace;

public class CTLChecker extends CheckerBase {
	private final CTL formula;

	public CTLChecker(final StateSpace s, final String formula)
			throws LtlParseException {
		this(s, s.getModel() instanceof EventBModel ? CTL.parseEventB(formula)
				: new CTL(formula));
	}

	public CTLChecker(final StateSpace s, final CTL formula) {
		this(s, formula, null);
	}

	public CTLChecker(final StateSpace s, final CTL formula, final IModelCheckListener ui) {
		super(s, ui);

		if (formula == null) {
			throw new IllegalArgumentException(
					"Cannot perform CTL checking without a correctly parsed CTL Formula");
		}

		this.formula = formula;
	}

	@Override
	protected void execute() {
		// Set the state limit to -1 (infinite) for now.
		// ProB's CTL checker cannot always resume checking after the state limit is reached,
		// so this could result in the same few states getting checked over and over again.
		// TODO Allow CtlCheckingCommand to return some sort of progress information during checking
		final CtlCheckingCommand cmd = new CtlCheckingCommand(this.getStateSpace(), formula, -1);
		this.getStateSpace().withTransaction(() -> this.getStateSpace().execute(cmd));
		this.isFinished(cmd.getResult(), null);
	}
}
