package de.prob.animator.command;

import java.util.Collections;
import java.util.List;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class GetVisBHtmlForStates extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_get_visb_html_for_states";
	
	private static final String HTML_ATOM_VARIABLE = "HTMLAtom";
	
	private final List<State> states;
	
	private String html;
	
	public GetVisBHtmlForStates(List<State> states) {
		this.states = states;
	}
	
	public GetVisBHtmlForStates(State state) {
		this(Collections.singletonList(state));
	}
	
	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		
		pto.openList();
		for (State state : this.states) {
			pto.printAtomOrNumber(state.getId());
		}
		pto.closeList();
		
		// Options list (currently we don't use it)
		pto.openList();
		pto.closeList();
		
		pto.printVariable(HTML_ATOM_VARIABLE);
		
		pto.closeTerm();
	}
	
	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		this.html = bindings.get(HTML_ATOM_VARIABLE).atomToString();
	}
	
	public String getHtml() {
		return this.html;
	}
}
