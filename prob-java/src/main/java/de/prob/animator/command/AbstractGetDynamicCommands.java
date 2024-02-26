package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.DynamicCommandItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public abstract class AbstractGetDynamicCommands extends AbstractCommand {

	static final String LIST = "List";
	private List<DynamicCommandItem> commands;
	final State id;
	private final String commandName;

	public AbstractGetDynamicCommands(State id, String commandName) {
		this.id = id;
		this.commandName = commandName;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(commandName);
		pto.printAtomOrNumber(id.getId());
		pto.printVariable(LIST);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		commands = BindingGenerator.getList(bindings, LIST).stream()
			.map(term -> DynamicCommandItem.fromPrologTerm(this.id, term))
			.collect(Collectors.toList());
	}

	public List<? extends DynamicCommandItem> getCommands() {
		return commands;
	}

}
