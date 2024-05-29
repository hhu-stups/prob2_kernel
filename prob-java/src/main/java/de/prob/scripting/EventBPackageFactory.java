package de.prob.scripting;

import com.google.common.io.MoreFiles;
import com.google.inject.Inject;
import com.google.inject.Provider;
import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.EventBPackageModel;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Named;
import de.prob.statespace.StateSpace;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EventBPackageFactory implements ModelFactory<EventBModel> {
	private static final class DummyElement extends AbstractElement implements Named {
		private String name;

		private DummyElement(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}
	
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

		final String componentName;
		if (EXTENSION.equals(MoreFiles.getFileExtension(file))) {
			componentName = MoreFiles.getNameWithoutExtension(file);
		} else {
			componentName = file.getFileName().toString();
		}

		// TODO: Extract machines, contexts, axioms, ...
		// Currently, the list of children is empty for EventBPackageModel
		EventBModel eventBModel = modelCreator.get().setLoadCommandPrologCode(loadcmd);
		return new ExtractedModel<>(stateSpaceProvider, eventBModel, new DummyElement(componentName));
	}
}
