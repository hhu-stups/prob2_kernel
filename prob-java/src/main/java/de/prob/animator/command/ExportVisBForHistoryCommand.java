package de.prob.animator.command;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.VisBExportOptions;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;

public class ExportVisBForHistoryCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_export_visb_html_for_history";

	private final List<String> transIDS;
	private final VisBExportOptions options;
	private final String path;

	public ExportVisBForHistoryCommand(final List<String> transIDS, final VisBExportOptions options, final String path) {
		this.transIDS = transIDS;
		this.options = options;
		this.path = path;
	}

	public ExportVisBForHistoryCommand(final Trace trace, final VisBExportOptions options, final Path path) {
		this(
				trace.getCurrentElements().stream()
						.filter(e -> e.getTransition() != null)
						.map(e -> e.getTransition().getId())
						.collect(Collectors.toList()),
				options,
				path.toString()
		);
	}

	public ExportVisBForHistoryCommand(final List<String> transIDS, final String path) {
		this(transIDS, VisBExportOptions.DEFAULT_HISTORY, path);
	}

	public ExportVisBForHistoryCommand(final Trace trace, final Path path) {
		this(trace, VisBExportOptions.DEFAULT_HISTORY, path);
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.openList();
		for (String transID : this.transIDS) {
			pto.printAtomOrNumber(transID);
		}
		pto.closeList();

		pto.openList();
		this.options.printProlog(pto);
		pto.closeList();

		pto.printAtom(path);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
