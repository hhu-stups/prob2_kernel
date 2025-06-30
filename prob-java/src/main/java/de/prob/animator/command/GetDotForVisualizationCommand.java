package de.prob.animator.command;

import java.io.File;
import java.util.List;

import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.animator.domainobjects.DynamicCommandItem;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.State;
import de.prob.statespace.Trace;

public class GetDotForVisualizationCommand extends AbstractDynamicVisualizationCommand<DotVisualizationCommand> {

	private static final String PROLOG_COMMAND_NAME = "call_dot_command_with_trace";

	@Deprecated
	public GetDotForVisualizationCommand(State state, DynamicCommandItem item, File file, List<IEvalElement> formulas) {
		this(state.getStateSpace().getTrace(state.getId()), (DotVisualizationCommand) item, file, formulas);
	}

	public GetDotForVisualizationCommand(Trace trace, DotVisualizationCommand item, File file, List<IEvalElement> formulas) {
		super(PROLOG_COMMAND_NAME, trace, item, file, formulas);
	}
}
