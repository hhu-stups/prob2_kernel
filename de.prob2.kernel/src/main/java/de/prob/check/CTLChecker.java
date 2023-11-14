package de.prob.check;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.CtlCheckingCommand;
import de.prob.animator.domainobjects.CTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.StateSpace;

public class CTLChecker extends CheckerBase {

	private final CTL formula;
	private final int stateLimit;

	public CTLChecker(final StateSpace s, final String formula) throws LtlParseException {
		this(s, s.getModel() instanceof EventBModel ? CTL.parseEventB(formula) : new CTL(formula));
	}

	public CTLChecker(final StateSpace s, final CTL formula) {
		this(s, formula, null);
	}

	public CTLChecker(final StateSpace s, final CTL formula, final IModelCheckListener ui) {
		// Set the state limit to -1 (infinite) per default.
		this(s, formula, ui, -1);
	}

	public CTLChecker(final StateSpace s, final CTL formula, final IModelCheckListener ui, final int stateLimit) {
		super(s, ui);

		if (formula == null) {
			throw new IllegalArgumentException("Cannot perform CTL checking without a correctly parsed CTL Formula");
		}
		this.formula = formula;

		if (stateLimit < 0) {
			this.stateLimit = -1;
		} else if (stateLimit > 0) {
			this.stateLimit = stateLimit;
		} else {
			throw new IllegalArgumentException("Cannot perform CTL checking with a stateLimit of 0");
		}
	}

	@Override
	protected void execute() {
		// TODO Allow CtlCheckingCommand to return some sort of progress information during checking
		final CtlCheckingCommand cmd = new CtlCheckingCommand(this.getStateSpace(), formula, stateLimit);
		this.getStateSpace().withTransaction(() -> this.getStateSpace().execute(cmd));
		this.isFinished(cmd.getResult(), null);
	}
}
