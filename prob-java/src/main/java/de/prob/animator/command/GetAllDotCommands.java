package de.prob.animator.command;

import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.Trace;

public class GetAllDotCommands extends AbstractGetDynamicCommands<DotVisualizationCommand> {

	private static final String PROLOG_COMMAND_NAME = "get_dot_commands_with_trace";

	@Deprecated
	public GetAllDotCommands(State state) {
		this(new Trace(state));
	}

	public GetAllDotCommands(Trace trace) {
		super(PROLOG_COMMAND_NAME, trace);
	}

	@Override
	protected DotVisualizationCommand createCommand(PrologTerm term) {
		return DotVisualizationCommand.fromPrologTerm(this.trace, term);
	}
}
