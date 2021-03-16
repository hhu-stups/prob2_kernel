package de.prob.animator.command;


import de.prob.animator.domainobjects.VisBItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.Map;

public class LoadVisBSetAttributesCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_attributes_for_state";

	private static final String ITEMS = "List";

	private final String stateID;

	private final Map<VisBItem.VisBItemKey, VisBItem> items;


	public LoadVisBSetAttributesCommand(final String stateID, final Map<VisBItem.VisBItemKey, VisBItem> items) {
		this.stateID = stateID;
		this.items = items;
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
		for(PrologTerm term : BindingGenerator.getList(bindings, ITEMS)) {
			BindingGenerator.getCompoundTerm(term, "set_attr", 3);
			final String id = PrologTerm.atomicString(term.getArgument(1));
			final String attribute = PrologTerm.atomicString(term.getArgument(2));
			final String value = PrologTerm.atomicString(term.getArgument(3));
			this.items.get(new VisBItem.VisBItemKey(id, attribute)).setValue(value);
		}
	}

}
