package de.prob.animator.command;

import de.prob.animator.domainobjects.PlantUmlVisualizationCommand;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

import java.util.List;
import java.util.stream.Collectors;

public class GetAllPlantUmlCommands extends AbstractGetDynamicCommands {

	static final String PROLOG_COMMAND_NAME = "get_plantuml_commands_in_state";

	private List<PlantUmlVisualizationCommand> commands;

	public GetAllPlantUmlCommands(State id) {
		super(id, PROLOG_COMMAND_NAME);
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.commands = BindingGenerator.getList(bindings, LIST).stream()
			.map(term -> PlantUmlVisualizationCommand.fromPrologTerm(this.id, term))
			.collect(Collectors.toList());
	}

	@Override
	public List<PlantUmlVisualizationCommand> getCommands() {
		return this.commands;
	}
}
