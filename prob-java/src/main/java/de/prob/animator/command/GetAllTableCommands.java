package de.prob.animator.command;

import de.prob.animator.domainobjects.TableVisualizationCommand;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;

public class GetAllTableCommands extends AbstractGetDynamicCommands<TableVisualizationCommand> {

	private static final String PROLOG_COMMAND_NAME = "get_table_commands_with_trace";

	public GetAllTableCommands(Trace trace) {
		super(PROLOG_COMMAND_NAME, trace);
	}

	@Override
	protected TableVisualizationCommand createCommand(PrologTerm term) {
		return TableVisualizationCommand.fromPrologTerm(this.trace, term);
	}
}
