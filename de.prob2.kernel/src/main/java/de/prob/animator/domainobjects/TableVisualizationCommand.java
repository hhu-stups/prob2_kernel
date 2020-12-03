package de.prob.animator.domainobjects;

import java.util.List;

import de.prob.animator.command.GetAllTableCommands;
import de.prob.animator.command.GetTableForVisualizationCommand;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class TableVisualizationCommand extends DynamicCommandItem {
	private TableVisualizationCommand(
		final State state,
		final String command,
		final String name,
		final String description,
		final int arity,
		final List<String> relevantPreferences,
		final List<PrologTerm> additionalInfo,
		final String available
	) {
		super(
			state,
			command,
			name,
			description,
			arity,
			relevantPreferences,
			additionalInfo,
			available
		);
	}
	
	public static TableVisualizationCommand fromPrologTerm(final State state, final PrologTerm term) {
		final DynamicCommandItem item = DynamicCommandItem.fromPrologTerm(state, term);
		
		return new TableVisualizationCommand(
			item.getState(),
			item.getCommand(),
			item.getName(),
			item.getDescription(),
			item.getArity(),
			item.getRelevantPreferences(),
			item.getAdditionalInfo(),
			item.getAvailable()
		);
	}
	
	public static List<TableVisualizationCommand> getAll(final State state) {
		final GetAllTableCommands cmd = new GetAllTableCommands(state);
		state.getStateSpace().execute(cmd);
		return cmd.getCommands();
	}
	
	public TableData visualize(final List<IEvalElement> formulas) {
		final GetTableForVisualizationCommand cmd = new GetTableForVisualizationCommand(this.getState(), this, formulas);
		this.getState().getStateSpace().execute(cmd);
		return cmd.getTable();
	}
}
