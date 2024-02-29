package de.prob.animator.command;


import de.prob.animator.domainobjects.VisBItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;


import java.util.List;
import java.util.stream.Collectors;

public class ReadVisBItemsCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_items";
	private static final String ITEMS = "Items";
	private List<VisBItem> items;

	public ReadVisBItemsCommand() {
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
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
