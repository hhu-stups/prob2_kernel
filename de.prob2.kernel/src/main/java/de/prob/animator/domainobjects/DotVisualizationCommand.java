package de.prob.animator.domainobjects;

import java.nio.file.Path;
import java.util.List;

import de.prob.animator.command.GetAllDotCommands;
import de.prob.animator.command.GetDotForVisualizationCommand;
import de.prob.animator.command.GetSvgForVisualizationCommand;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class DotVisualizationCommand extends DynamicCommandItem {
	private DotVisualizationCommand(
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
	
	public static DotVisualizationCommand fromPrologTerm(final State state, final PrologTerm term) {
		final DynamicCommandItem item = DynamicCommandItem.fromPrologTerm(state, term);
		
		return new DotVisualizationCommand(
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
	
	public static List<DotVisualizationCommand> getAll(final State state) {
		final GetAllDotCommands cmd = new GetAllDotCommands(state);
		state.getStateSpace().execute(cmd);
		return cmd.getCommands();
	}
	
	public void visualizeAsDotToFile(final Path dotFilePath, final List<IEvalElement> formulas) {
		final GetDotForVisualizationCommand cmd = new GetDotForVisualizationCommand(this.getState(), this, dotFilePath.toFile(), formulas);
		this.getState().getStateSpace().execute(cmd);
	}
	
	public void visualizeAsSvgToFile(final Path svgFilePath, final List<IEvalElement> formulas) {
		final GetSvgForVisualizationCommand cmd = new GetSvgForVisualizationCommand(this.getState(), this, svgFilePath.toFile(), formulas);
		this.getState().getStateSpace().execute(cmd);
	}
}
