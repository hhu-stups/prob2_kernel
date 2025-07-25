package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class SerializeStateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "serialize";
	private static final String VARIABLE = "State";

	private final String id;
	private String state;

	public SerializeStateCommand(final String id) {
		this.id = id;
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		state = bindings.get(VARIABLE).atomToString();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME)
				.printAtomOrNumber(id)
				.printVariable(VARIABLE)
				.closeTerm();
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

}
