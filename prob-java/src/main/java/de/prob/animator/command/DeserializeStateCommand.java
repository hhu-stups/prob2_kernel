package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Transition;

public class DeserializeStateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "deserialize";
	private static final String VARIABLE = "Id";

	private String id;
	private final String state;

	public DeserializeStateCommand(final String state) {
		this.state = state;
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		id = Transition.getIdFromPrologTerm(bindings.get(VARIABLE));
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME)
				.printVariable(VARIABLE)
				.printAtom(state)
				.closeTerm();
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

}
