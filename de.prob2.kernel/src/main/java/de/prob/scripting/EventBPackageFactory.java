package de.prob.scripting;

import com.google.common.io.MoreFiles;
import com.google.inject.Inject;
import com.google.inject.Provider;
import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.EventBPackageModel;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Named;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	
	private final Provider<EventBPackageModel> modelCreator;

	@Inject
	public EventBPackageFactory(final Provider<EventBPackageModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	private List<String> readFile(final File machine) throws IOException {
		try (final Stream<String> lines = Files.lines(machine.toPath())) {
			return lines.collect(Collectors.toList());
		}
	}
	
	@Override
	public ExtractedModel<EventBModel> extract(final String fileName) throws IOException {
		final Pattern pattern = Pattern.compile("^package\\((.*?)\\)\\.");
		final File file = new File(fileName);
		final List<String> lines = readFile(file);
		String loadcmd = null;
		for (final String string : lines) {
			final Matcher m1 = pattern.matcher(string);
			if (m1.find()) {
				loadcmd = m1.group(1);
			}
		}
		if (loadcmd == null) {
			throw new IllegalArgumentException(fileName + " contained no valid Event-B Load command");
		}

		final Path path = Paths.get(fileName);
		final String componentName;
		if (EXTENSION.equals(MoreFiles.getFileExtension(path))) {
			componentName = MoreFiles.getNameWithoutExtension(path);
		} else {
			componentName = path.getFileName().toString();
		}
		// TODO: Extract machines, contexts, axioms, ...
		// Currently, the list of children is empty for EventBPackageModel
		EventBModel eventBModel = modelCreator.get().setLoadCommandPrologCode(loadcmd);
		return new ExtractedModel<>(eventBModel, new DummyElement(componentName));
	}
}
