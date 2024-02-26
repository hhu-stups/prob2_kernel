package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetTopLevelFormulasCommand extends AbstractCommand { 
	private static final String PROLOG_COMMAND_NAME = "get_top_level_formulas";
	private static final String TOP_IDS = "TopIds";

	private final List<String> ids;

	public GetTopLevelFormulasCommand() {
		this.ids = new ArrayList<>();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(TOP_IDS);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		ids.addAll(PrologTerm.atomsToStrings(BindingGenerator.getList(bindings, TOP_IDS)));
	}

	public List<String> getIds() {
		return Collections.unmodifiableList(this.ids);
	}
}
