package de.prob.animator.domainobjects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.prob.animator.command.GetAllPlantUmlCommands;
import de.prob.animator.command.GetPlantUmlForVisualizationCommand;
import de.prob.exception.ProBError;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlantUmlVisualizationCommand extends DynamicCommandItem {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlantUmlVisualizationCommand.class);

	public static final String SEQUENCE_CHART = "uml_sequence_chart";

	private PlantUmlVisualizationCommand(
			final Trace trace,
			final String command,
			final String name,
			final String description,
			final int arity,
			final List<String> relevantPreferences,
			final List<PrologTerm> additionalInfo,
			final String available
	) {
		super(
				trace,
				command,
				name,
				description,
				arity,
				relevantPreferences,
				additionalInfo,
				available
		);
	}

	public static PlantUmlVisualizationCommand fromPrologTerm(final Trace trace, final PrologTerm term) {
		final DynamicCommandItem item = DynamicCommandItem.fromPrologTerm(trace, term);
		return new PlantUmlVisualizationCommand(
				item.getTrace(),
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
	 * Get a list of information about all supported plantuml visualization commands.
	 *
	 * @param trace the state with which the commands should be executed when called
	 * @return information about all supported plantuml visualization commands
	 */
	public static List<PlantUmlVisualizationCommand> getAll(final Trace trace) {
		final GetAllPlantUmlCommands cmd = new GetAllPlantUmlCommands(trace);
		trace.getStateSpace().execute(cmd);
		return cmd.getCommands();
	}
	
	/**
	 * Get information about a specific plantuml visualization command by name.
	 * Some common plantuml visualization command names are defined as constants in {@link PlantUmlVisualizationCommand}.
	 * 
	 * @param commandName the name of the command to look up
	 * @param trace the trace with which the command should be executed when called
	 * @return information about the named plantuml visualization command
	 */
	public static PlantUmlVisualizationCommand getByName(final String commandName, final Trace trace) {
		return getAll(trace).stream()
			.filter(command -> commandName.equals(command.getCommand()))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Could not find plantuml visualization command named " + commandName));
	}
	
	/**
	 * Execute this visualization command and write the generated graph as plantuml source code into the given file.
	 * 
	 * @param pumlFilePath the file into which to write the generated plantuml source code
	 * @param formulas arguments for the command, if it takes any
	 */
	public void visualizeAsPlantUmlToFile(final Path pumlFilePath, final List<IEvalElement> formulas) {
		final GetPlantUmlForVisualizationCommand cmd = new GetPlantUmlForVisualizationCommand(this.getTrace(), this, pumlFilePath.toFile(), formulas);
		this.getTrace().getStateSpace().execute(cmd);
	}
	
	/**
	 * Execute this visualization command and return the generated graph as plantuml source code as a byte array.
	 *
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph as plantuml source code
	 */
	public byte[] visualizeAsPlantUmlToBytes(final List<IEvalElement> formulas) {
		final Path tempPumlFile;
		try {
			tempPumlFile = Files.createTempFile("probjava", ".puml");
		} catch (IOException e) {
			throw new ProBError("Failed to create temporary plantuml file", e);
		}
		try {
			this.visualizeAsPlantUmlToFile(tempPumlFile, formulas);
			return Files.readAllBytes(tempPumlFile);
		} catch (IOException e) {
			throw new ProBError("Failed to read temporary plantuml file", e);
		} finally {
			try {
				Files.delete(tempPumlFile);
			} catch (IOException e) {
				LOGGER.error("Failed to delete temporary plantuml file", e);
			}
		}
	}
	
	/**
	 * Execute this visualization command and return the generated graph as plantuml source code as a string.
	 *
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph as plantuml source code
	 */
	public String visualizeAsPlantUmlToString(final List<IEvalElement> formulas) {
		return new String(this.visualizeAsPlantUmlToBytes(formulas), StandardCharsets.UTF_8);
	}
}
