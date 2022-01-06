package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.List;

public class ExportVisBForHistoryCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_export_visb_html_for_history";

	private final List<String> transIDS;

	private final String path;

	public ExportVisBForHistoryCommand(final List<String> transIDS, final String path) {
		this.transIDS = transIDS;
		this.path = path;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.openList();
		for(String transID : transIDS) {
			pto.printAtomOrNumber(transID);
		}
		pto.closeList();
		pto.printAtom(path);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}

}
