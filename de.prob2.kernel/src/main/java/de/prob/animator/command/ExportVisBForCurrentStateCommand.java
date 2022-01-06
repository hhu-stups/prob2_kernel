package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class ExportVisBForCurrentStateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_export_visb_for_current_state";

	private final String path;

	public ExportVisBForCurrentStateCommand(final String path) {
		this.path = path;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(path);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}

}
