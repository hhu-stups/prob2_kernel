package de.prob.animator.domainobjects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.prob.animator.CommandInterruptedException;
import de.prob.animator.command.GetAllDotCommands;
import de.prob.animator.command.GetDotForVisualizationCommand;
import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.Trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DotVisualizationCommand extends DynamicCommandItem {

	private static final Logger LOGGER = LoggerFactory.getLogger(DotVisualizationCommand.class);

	public static final String CUSTOM_GRAPH_NAME = "custom_graph";
	public static final String EXPRESSION_AS_GRAPH_NAME = "expr_as_graph";
	public static final String FORMULA_TREE_NAME = "formula_tree";
	public static final String RULE_DEPENDENCY_GRAPH_NAME = "rule_dependency_graph";
	public static final String STATE_AS_GRAPH_NAME = "state_as_graph";
	public static final String STATE_SPACE_NAME = "state_space";
	public static final String STATE_SPACE_FAST_NAME = "state_space_sfdp";
	public static final String STATE_SPACE_PROJECTION_NAME = "transition_diagram";
	
	private DotVisualizationCommand(
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

	@Deprecated
	public static DotVisualizationCommand fromPrologTerm(final State state, final PrologTerm term) {
		return fromPrologTerm(state.getStateSpace().getTrace(state.getId()), term);
	}

	public static DotVisualizationCommand fromPrologTerm(final Trace trace, final PrologTerm term) {
		final DynamicCommandItem item = DynamicCommandItem.fromPrologTerm(trace, term);
		return new DotVisualizationCommand(
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

	@Deprecated
	public static List<DotVisualizationCommand> getAll(final State state) {
		return getAll(state.getStateSpace().getTrace(state.getId()));
	}

	/**
	 * Get a list of information about all supported dot visualization commands.
	 *
	 * @param trace the trace with which the commands should be executed when called
	 * @return information about all supported dot visualization commands
	 */
	public static List<DotVisualizationCommand> getAll(final Trace trace) {
		final GetAllDotCommands cmd = new GetAllDotCommands(trace);
		trace.getStateSpace().execute(cmd);
		return cmd.getCommands();
	}

	@Deprecated
	public static DotVisualizationCommand getByName(final String commandName, final State state) {
		return getByName(commandName, state.getStateSpace().getTrace(state.getId()));
	}
	
	/**
	 * Get information about a specific dot visualization command by name.
	 * Some common dot visualization command names are defined as constants in {@link DotVisualizationCommand}.
	 * 
	 * @param commandName the name of the command to look up
	 * @param trace the trace with which the command should be executed when called
	 * @return information about the named dot visualization command
	 */
	public static DotVisualizationCommand getByName(final String commandName, final Trace trace) {
		return getAll(trace).stream()
			.filter(command -> commandName.equals(command.getCommand()))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("Could not find dot visualization command named " + commandName));
	}
	
	public Optional<String> getPreferredDotLayoutEngine() {
		return this.getAdditionalInfo().stream()
			.filter(t -> "preferred_dot_type".equals(t.getFunctor()))
			.map(t -> BindingGenerator.getCompoundTerm(t, 1))
			.map(t -> t.getArgument(1).atomToString())
			.findAny();
	}
	
	/**
	 * Execute this visualization command and write the generated graph as dot source code into the given file.
	 * 
	 * @param dotFilePath the file into which to write the generated dot source code
	 * @param formulas arguments for the command, if it takes any
	 */
	public void visualizeAsDotToFile(final Path dotFilePath, final List<IEvalElement> formulas) {
		final GetDotForVisualizationCommand cmd = new GetDotForVisualizationCommand(this.getTrace(), this, dotFilePath.toFile(), formulas);
		this.getTrace().getStateSpace().execute(cmd);
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
			tempDotFile = Files.createTempFile("probjava", ".dot");
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
	 * <p>
	 * Execute this visualization command and return the generated graph in the requested output format as a byte array.
	 * {@link DotLayoutEngine} provides constants for common output format names.
	 * </p>
	 * <p>
	 * This internally generates the graph in dot format and uses the dot command to convert it to the requested format.
	 * If you need more control about how dot is called,
	 * use {@link #visualizeAsDotToBytes(List)} to generate the source code and {@link DotCall} to convert it.
	 * </p>
	 *
	 * @param outputFormat the format in which to output the generated graph
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph in the requested format
	 */
	public byte[] visualizeToBytes(final String outputFormat, final List<IEvalElement> formulas) {
		final DotCall dotCall = new DotCall(this.getTrace().getStateSpace().getCurrentPreference("DOT"))
			.layoutEngine(this.getPreferredDotLayoutEngine().orElseGet(() ->
					                                                           this.getTrace().getStateSpace().getCurrentPreference("DOT_ENGINE")
			))
			.outputFormat(outputFormat)
			.input(this.visualizeAsDotToBytes(formulas));
		try {
			return dotCall.call();
		} catch (InterruptedException e) {
			throw new CommandInterruptedException("dot call interrupted", Collections.emptyList(), e);
		}
	}
	
	/**
	 * Execute this visualization command and write the generated graph in SVG format into the given file.
	 *
	 * @param svgFilePath the file into which to write the generated dot source code
	 * @param formulas arguments for the command, if it takes any
	 */
	public void visualizeAsSvgToFile(final Path svgFilePath, final List<IEvalElement> formulas) {
		final byte[] svgData = this.visualizeToBytes(DotOutputFormat.SVG, formulas);
		try {
			Files.write(svgFilePath, svgData);
		} catch (IOException e) {
			throw new ProBError("Failed to write SVG data to file", e);
		}
	}
	
	/**
	 * Execute this visualization command and return the generated graph in SVG format as a string.
	 *
	 * @param formulas arguments for the command, if it takes any
	 * @return the generated graph in SVG format
	 */
	public String visualizeAsSvgToString(final List<IEvalElement> formulas) {
		return new String(this.visualizeToBytes(DotOutputFormat.SVG, formulas), StandardCharsets.UTF_8);
	}
}
