package de.prob.animator.command;

import de.prob.animator.domainobjects.PlantUmlVisualizationCommand;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;

public class GetAllPlantUmlCommands extends AbstractGetDynamicCommands<PlantUmlVisualizationCommand> {

	private static final String PROLOG_COMMAND_NAME = "get_plantuml_commands_with_trace";

	public GetAllPlantUmlCommands(Trace trace) {
		super(PROLOG_COMMAND_NAME, trace);
	}

	@Override
	protected PlantUmlVisualizationCommand createCommand(PrologTerm term) {
		return PlantUmlVisualizationCommand.fromPrologTerm(this.trace, term);
	}
}
