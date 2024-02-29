package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.TableVisualizationCommand;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public class GetAllTableCommands extends AbstractGetDynamicCommands {

	static final String PROLOG_COMMAND_NAME = "get_table_commands_in_state";

	private List<TableVisualizationCommand> commands;

	public GetAllTableCommands(State id) {
		super(id, PROLOG_COMMAND_NAME);
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.commands = BindingGenerator.getList(bindings, LIST).stream()
			.map(term -> TableVisualizationCommand.fromPrologTerm(this.id, term))
			.collect(Collectors.toList());
	}

	@Override
	public List<TableVisualizationCommand> getCommands() {
		return this.commands;
	}
}
