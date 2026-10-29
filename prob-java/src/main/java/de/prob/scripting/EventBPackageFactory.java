package de.prob.scripting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.EventBPackageModel;
import de.prob.statespace.StateSpace;

public final class EventBPackageFactory implements ModelFactory<EventBModel> {
	public static final String EXTENSION = "eventb";
	private static final Pattern PACKAGE_START = Pattern.compile("^package\\(", Pattern.MULTILINE);
	
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<EventBPackageModel> modelCreator;

	@Inject
	EventBPackageFactory(Provider<StateSpace> stateSpaceProvider, Provider<EventBPackageModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<EventBModel> extract(final String fileName) throws IOException {
		final Path file = Paths.get(fileName);
		final String loadcmd = extractLoadCommand(Files.readString(file));
		if (loadcmd == null) {
			throw new IllegalArgumentException(file + " contained no valid Event-B Load command");
		}

		// TODO: Extract machines, contexts, axioms, ...
		// Currently, the list of children is empty for EventBPackageModel
		EventBModel eventBModel = modelCreator.get().create(file, loadcmd);
		return new ExtractedModel<>(stateSpaceProvider, eventBModel);
	}

	/**
	 * Extracts the argument of the {@code package(...).} term.
	 * The term ends at the last {@code ).} in the file and not at the first one,
	 * because quoted atoms (e.g. descriptions) may themselves contain {@code ).}.
	 * Stopping at such an inner occurrence would yield a truncated term with an unterminated quote,
	 * on which the Prolog reader blocks waiting for more input.
	 *
	 * @return the argument of the package term, or {@code null} if there is none
	 */
	static String extractLoadCommand(final String content) {
		final Matcher matcher = PACKAGE_START.matcher(content);
		if (!matcher.find()) {
			return null;
		}
		final int start = matcher.end();
		final int end = content.lastIndexOf(").");
		if (end < start) {
			return null;
		}
		return content.substring(start, end);
	}
}
