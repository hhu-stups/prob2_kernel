package de.prob.animator.command;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.prob.animator.domainobjects.DotVisualizationCommand;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

/**
 * @deprecated Use {@link DotVisualizationCommand#getByName(String, State)}, {@link DotVisualizationCommand#STATE_AS_GRAPH_NAME}, and {@link DotVisualizationCommand#visualizeAsDotToFile(Path, List)} instead.
 */
@Deprecated
public class GetDotForStateVizCmd extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "write_dot_for_state_viz";

	private final State id;
	private final File tempFile;
	private String content;

	public GetDotForStateVizCmd(State id) {
		this.id = id;
		try {
			tempFile = File.createTempFile("dotSM", ".dot");
			tempFile.deleteOnExit();
		} catch (IOException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(id.getId());
		pto.printAtom(tempFile.getAbsolutePath());
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings){
		try (final Stream<String> lines = Files.lines(tempFile.toPath())) {
			content = lines.collect(Collectors.joining("\n"));
		} catch (UncheckedIOException | IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getContent() {
		return content;
	}
}
