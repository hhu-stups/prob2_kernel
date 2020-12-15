package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public class GetAllDotCommands extends AbstractGetDynamicCommands {

	static final String PROLOG_COMMAND_NAME = "get_dot_commands_in_state";

	private List<DotVisualizationCommand> commands;

	public GetAllDotCommands(State id) {
		super(id, PROLOG_COMMAND_NAME);
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.commands = BindingGenerator.getList(bindings, LIST).stream()
			.map(term -> DotVisualizationCommand.fromPrologTerm(this.id, term))
			.collect(Collectors.toList());
	}

	@Override
	public List<DotVisualizationCommand> getCommands() {
		return this.commands;
	}
}
