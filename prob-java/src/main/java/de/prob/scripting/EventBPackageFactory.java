package de.prob.scripting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.EventBPackageModel;
import de.prob.statespace.StateSpace;

public final class EventBPackageFactory implements ModelFactory<EventBModel> {
	public static final String EXTENSION = "eventb";
	
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<EventBPackageModel> modelCreator;

	@Inject
	EventBPackageFactory(Provider<StateSpace> stateSpaceProvider, Provider<EventBPackageModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	private List<String> readFile(final Path machine) throws IOException {
		try {
			return Files.readAllLines(machine);
		} catch (UncheckedIOException e) {
			// the stream will throw an UncheckedIOException when there is an IOException while reading
			throw e.getCause();
		}
	}
	
	@Override
	public ExtractedModel<EventBModel> extract(final String fileName) throws IOException {
		final Pattern pattern = Pattern.compile("^package\\((.*?)\\)\\.");
		final Path file = Paths.get(fileName);
		final List<String> lines = readFile(file);
		String loadcmd = null;
		for (final String string : lines) {
			final Matcher m1 = pattern.matcher(string);
			if (m1.find()) {
				loadcmd = m1.group(1);
			}
		}
		if (loadcmd == null) {
			throw new IllegalArgumentException(file + " contained no valid Event-B Load command");
		}

		// TODO: Extract machines, contexts, axioms, ...
		// Currently, the list of children is empty for EventBPackageModel
		EventBModel eventBModel = modelCreator.get().create(file, loadcmd);
		return new ExtractedModel<>(stateSpaceProvider, eventBModel);
	}
}
