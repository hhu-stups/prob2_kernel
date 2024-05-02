package de.prob.animator.command;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;

public class ExportVisBForHistoryCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_export_visb_html_for_history";

	private final List<String> transIDS;

	private final String path;

	public ExportVisBForHistoryCommand(final List<String> transIDS, final String path) {
		this.transIDS = transIDS;
		this.path = path;
	}

	public ExportVisBForHistoryCommand(Trace trace, Path path) {
		this(
				trace.getCurrentElements().stream()
						.filter(e -> e.getTransition() != null)
						.map(e -> e.getTransition().getId())
						.collect(Collectors.toList()),
				path.toString()
		);
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.openList();
		for (String transID : this.transIDS) {
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
