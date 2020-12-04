package de.prob.animator.domainobjects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.prob.animator.command.GetAllDotCommands;
import de.prob.animator.command.GetDotForVisualizationCommand;
import de.prob.animator.command.GetSvgForVisualizationCommand;
import de.prob.exception.ProBError;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DotVisualizationCommand extends DynamicCommandItem {
	private static final Logger LOGGER = LoggerFactory.getLogger(DotVisualizationCommand.class);
	
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
	
	/**
	 * Execute this visualization command and write the generated graph as dot source code into the given file.
	 * 
	 * @param dotFilePath the file into which to write the generated dot source code
	 * @param formulas arguments for the command, if it takes any
	 */
	public void visualizeAsDotToFile(final Path dotFilePath, final List<IEvalElement> formulas) {
		final GetDotForVisualizationCommand cmd = new GetDotForVisualizationCommand(this.getState(), this, dotFilePath.toFile(), formulas);
		this.getState().getStateSpace().execute(cmd);
	}
	
	/**
	 * Execute this visualization command and return the generated graph as dot source code as a byte array.
	 *
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph as dot source code
	 */
	public byte[] visualizeAsDotToBytes(final List<IEvalElement> formulas) {
		final Path tempDotFile;
		try {
			tempDotFile = Files.createTempFile("prob2", ".dot");
		} catch (IOException e) {
			throw new ProBError("Failed to create temporary dot file", e);
		}
		try {
			this.visualizeAsDotToFile(tempDotFile, formulas);
			return Files.readAllBytes(tempDotFile);
		} catch (IOException e) {
			throw new ProBError("Failed to read temporary dot file", e);
		} finally {
			try {
				Files.delete(tempDotFile);
			} catch (IOException e) {
				LOGGER.error("Failed to delete temporary dot file", e);
			}
		}
	}
	
	/**
	 * Execute this visualization command and return the generated graph as dot source code as a string.
	 * If you only need to pass the source code to {@link DotCall},
	 * it's recommended to use {@link #visualizeAsDotToBytes(List)} instead,
	 * to avoid encoding errors.
	 *
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph as dot source code
	 */
	public String visualizeAsDotToString(final List<IEvalElement> formulas) {
		return new String(this.visualizeAsDotToBytes(formulas), StandardCharsets.UTF_8);
	}
	
	/**
	 * Execute this visualization command and write the generated graph in SVG format into the given file.
	 *
	 * @param svgFilePath the file into which to write the generated dot source code
	 * @param formulas arguments for the command, if it takes any
	 */
	public void visualizeAsSvgToFile(final Path svgFilePath, final List<IEvalElement> formulas) {
		final GetSvgForVisualizationCommand cmd = new GetSvgForVisualizationCommand(this.getState(), this, svgFilePath.toFile(), formulas);
		this.getState().getStateSpace().execute(cmd);
	}
	
	/**
	 * Execute this visualization command and return the generated graph in SVG format as a string.
	 *
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph in SVG format
	 */
	public String visualizeAsSvgToString(final List<IEvalElement> formulas) {
		final Path tempSvgFile;
		try {
			tempSvgFile = Files.createTempFile("prob2", ".svg");
		} catch (IOException e) {
			throw new ProBError("Failed to create temporary SVG file", e);
		}
		try {
			this.visualizeAsSvgToFile(tempSvgFile, formulas);
			return new String(Files.readAllBytes(tempSvgFile), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ProBError("Failed to read temporary SVG file", e);
		} finally {
			try {
				Files.delete(tempSvgFile);
			} catch (IOException e) {
				LOGGER.error("Failed to delete temporary SVG file", e);
			}
		}
	}
}
