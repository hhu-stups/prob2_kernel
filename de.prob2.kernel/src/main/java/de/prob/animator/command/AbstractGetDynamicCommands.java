package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import de.prob.animator.domainobjects.DynamicCommandItem;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public abstract class AbstractGetDynamicCommands extends AbstractCommand {

	private static final String LIST = "List";
	private List<DynamicCommandItem> commands = new ArrayList<>();
	private final State id;
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
		ListPrologTerm res = (ListPrologTerm) bindings.get(LIST);
		for (PrologTerm prologTerm : res) {
			commands.add(DynamicCommandItem.fromPrologTerm(prologTerm));
		}		
	}

	public List<DynamicCommandItem> getCommands() {
		return commands;
	}

}
