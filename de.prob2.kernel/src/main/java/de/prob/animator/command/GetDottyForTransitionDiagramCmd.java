package de.prob.animator.command;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetDottyForTransitionDiagramCmd extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "write_dotty_transition_diagram";

	private final String expression;
	private final File tempFile;
	private String content;

	public GetDottyForTransitionDiagramCmd(String expr) {
		expression = expr;
		try {
			tempFile = File.createTempFile("dotTD", ".dot");
			tempFile.deleteOnExit();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(expression);
		pto.printAtom(tempFile.getAbsolutePath());
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
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
