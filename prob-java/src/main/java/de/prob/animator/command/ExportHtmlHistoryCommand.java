package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ExportHtmlHistoryCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_save_html_history";
	private final File file;
	private final List<String> transitionIds;

	public ExportHtmlHistoryCommand(final File file, final List<String> transitionIds) {
		this.file = file;
		this.transitionIds = transitionIds;
	}

	public ExportHtmlHistoryCommand(final File file, final Trace trace) {
		this(file, trace.getTransitionList().stream().map(Transition::getId).collect(Collectors.toList()));
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(file.getAbsolutePath());
		pto.openList();
		transitionIds.forEach(pto::printAtomOrNumber);
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}

}
