package de.prob.animator.domainobjects;

import java.util.List;

import de.prob.animator.command.GetAllTableCommands;
import de.prob.animator.command.GetTableForVisualizationCommand;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class TableVisualizationCommand extends DynamicCommandItem {
	public static final String EXPRESSION_AS_TABLE_NAME = "expr_as_table";
	public static final String UNSAT_CORE_PROPERTIES_NAME = "unsat_core_properties";
	
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
	
	/**
	 * Get a list of information about all supported table visualization commands.
	 * 
	 * @param state the state in which the commands should be executed when called
	 * @return information about all supported table visualization commands
	 */
	public static List<TableVisualizationCommand> getAll(final State state) {
		final GetAllTableCommands cmd = new GetAllTableCommands(state);
		state.getStateSpace().execute(cmd);
		return cmd.getCommands();
	}
	
	/**
	 * Get information about a specific table visualization command by name.
	 * Some common table visualization command names are defined as constants in {@link TableVisualizationCommand}.
	 * 
	 * @param commandName the name of the command to look up
	 * @param state the state in which the command should be executed when called
	 * @return information about the named table visualization command
	 */
	public static TableVisualizationCommand getByName(final String commandName, final State state) {
		return getAll(state).stream()
			.filter(command -> commandName.equals(command.getCommand()))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Could not find table visualization command named " + commandName));
	}
	
	public TableData visualize(final List<IEvalElement> formulas) {
		final GetTableForVisualizationCommand cmd = new GetTableForVisualizationCommand(this.getState(), this, formulas);
		this.getState().getStateSpace().execute(cmd);
		return cmd.getTable();
	}
}
