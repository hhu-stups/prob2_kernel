package de.prob.check;

import de.be4.ltl.core.parser.LtlParseException;
import de.prob.animator.command.LtlCheckingCommand;
import de.prob.animator.domainobjects.LTL;
import de.prob.model.eventb.EventBModel;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

public class LTLChecker extends CheckerBase {

	private final LTL formula;
	private final int stateLimit;
	private final State startState;

	public LTLChecker(final StateSpace s, final String formula) throws LtlParseException {
		this(s, s.getModel() instanceof EventBModel ? LTL.parseEventB(formula) : new LTL(formula));
	}

	public LTLChecker(final StateSpace s, final LTL formula) {
		this(s, formula, null);
	}

	public LTLChecker(final StateSpace s, final LTL formula, final IModelCheckListener ui) {
		// Set the state limit to -1 (infinite) per default.
		this(s, formula, ui, -1);
	}

	public LTLChecker(final StateSpace s, final LTL formula, final IModelCheckListener ui, final int stateLimit) {
		this(s, formula, ui, stateLimit, null);
	}

	public LTLChecker(StateSpace s, LTL formula, IModelCheckListener ui, int stateLimit, State startState) {
		super(s, ui);

		if (formula == null) {
			throw new IllegalArgumentException("Cannot perform LTL checking without a correctly parsed LTL Formula");
		}
		this.formula = formula;

		if (stateLimit < 0) {
			this.stateLimit = -1;
		} else if (stateLimit > 0) {
			this.stateLimit = stateLimit;
		} else {
			throw new IllegalArgumentException("Cannot perform LTL checking with a stateLimit of 0");
		}

		this.startState = startState;
	}

	@Override
	protected void execute() {
		// TODO Allow LtlCheckingCommand to return some sort of progress information during checking
		final LtlCheckingCommand cmd = new LtlCheckingCommand(this.getStateSpace(), formula, stateLimit, startState);
		this.getStateSpace().withTransaction(() -> this.getStateSpace().execute(cmd));
		this.isFinished(cmd.getResult(), null);
	}
}
