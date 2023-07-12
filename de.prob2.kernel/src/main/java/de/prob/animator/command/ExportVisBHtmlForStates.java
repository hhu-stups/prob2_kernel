package de.prob.animator.command;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import de.prob.animator.domainobjects.VisBExportOptions;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class ExportVisBHtmlForStates extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_export_visb_html_for_states";
	
	private final List<State> states;
	private final VisBExportOptions options;
	private final Path path;
	
	public ExportVisBHtmlForStates(List<State> states, VisBExportOptions options, Path path) {
		this.states = states;
		this.options = options;
		this.path = path;
	}
	
	public ExportVisBHtmlForStates(List<State> states, Path path) {
		this(states, VisBExportOptions.DEFAULT, path);
	}
	
	public ExportVisBHtmlForStates(State state, VisBExportOptions options, Path path) {
		this(Collections.singletonList(state), options, path);
	}
	
	public ExportVisBHtmlForStates(State state, Path path) {
		this(Collections.singletonList(state), path);
	}
	
	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		
		pto.openList();
		for (State state : this.states) {
			pto.printAtomOrNumber(state.getId());
		}
		pto.closeList();
		
		pto.printAtom(this.path.toString());
		
		pto.openList();
		this.options.printProlog(pto);
		pto.closeList();
		
		pto.closeTerm();
	}
	
	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
