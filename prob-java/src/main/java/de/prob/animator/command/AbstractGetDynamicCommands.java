package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.DynamicCommandItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public abstract class AbstractGetDynamicCommands<T extends DynamicCommandItem> extends AbstractCommand {

	private static final String LIST = "List";

	private final String commandName;
	protected final Trace trace;
	private List<T> commands;

	protected AbstractGetDynamicCommands(String commandName, Trace trace) {
		this.commandName = commandName;
		this.trace = trace;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(this.commandName);
		pto.openList();
		// TODO: do we want to ignore the forward history?
		for (Transition t : this.trace.getTransitionList()) {
			pto.printAtomOrNumber(t.getId());
		}
		pto.closeList();
		pto.printVariable(LIST);
		pto.closeTerm();
	}

	protected abstract T createCommand(PrologTerm term);

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		commands = BindingGenerator.getList(bindings, LIST).stream()
				           .map(this::createCommand)
			.collect(Collectors.toList());
	}

	public List<T> getCommands() {
		return commands;
	}
}
