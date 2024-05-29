package de.prob.scripting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.CSPModel;
import de.prob.statespace.StateSpace;

public class CSPFactory implements ModelFactory<CSPModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<CSPModel> modelCreator;
	
	@Inject
	CSPFactory(Provider<StateSpace> stateSpaceProvider, Provider<CSPModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<CSPModel> extract(final String modelPath) throws IOException {
		CSPModel cspModel = modelCreator.get();
		Path p = Paths.get(modelPath);

		final String text;
		try (final Stream<String> lines = Files.lines(p)) {
			text = lines.collect(Collectors.joining("\n"));
		} catch (UncheckedIOException e) {
			// the stream will throw an UncheckedIOException when there is an IOException while reading
			throw e.getCause();
		}

		cspModel = cspModel.create(text, p.toFile());
		return new ExtractedModel<>(stateSpaceProvider, cspModel);
	}
}
