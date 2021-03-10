package de.prob.animator.command;


import de.prob.animator.domainobjects.VisBItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.List;
import java.util.stream.Collectors;

public class LoadVisBSetAttributesCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_attributes_for_state";

	private static final String ITEMS = "List";

	private String stateID;

	private List<VisBItem> items;


	public LoadVisBSetAttributesCommand(String stateID) {
		this.stateID = stateID;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(stateID);
		pto.printVariable(ITEMS);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.items = BindingGenerator.getList(bindings, ITEMS).stream()
				.map(VisBItem::fromPrologTerm)
				.collect(Collectors.toList());
	}

	public List<VisBItem> getItems() {
		return items;
	}
}
