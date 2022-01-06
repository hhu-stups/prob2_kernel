package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class ResetProBCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_reset_prob";

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.printAtom(PROLOG_COMMAND_NAME);
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
