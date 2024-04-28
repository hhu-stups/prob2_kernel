package de.prob.animator.command;

import java.io.File;
import java.util.List;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.PlantUmlVisualizationCommand;
import de.prob.statespace.Trace;

public class GetPlantUmlForVisualizationCommand extends AbstractDynamicVisualizationCommand<PlantUmlVisualizationCommand> {

	private static final String PROLOG_COMMAND_NAME = "call_plantuml_command_with_trace";

	public GetPlantUmlForVisualizationCommand(Trace trace, PlantUmlVisualizationCommand item, File file, List<IEvalElement> formulas) {
		super(PROLOG_COMMAND_NAME, trace, item, file, formulas);
	}
}
