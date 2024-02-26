package de.prob.animator.command;

/**
 * Checks if constants are set up in the given state.
 */
public final class CheckConstantsSetUpStatusCommand extends CheckBooleanPropertyCommand {
	private static final String PROPERTY_NAME = "constants_set_up";

	public CheckConstantsSetUpStatusCommand(final String stateId) {
		super(PROPERTY_NAME, stateId);
	}

	public boolean isConstantsSetUp() {
		return this.getResult();
	}
}
