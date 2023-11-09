package de.prob.scripting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.CSPModel;

public class CSPFactory implements ModelFactory<CSPModel> {
	private final Provider<CSPModel> modelCreator;
	
	@Inject
	public CSPFactory(final Provider<CSPModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<CSPModel> extract(final String modelPath) throws IOException {
		CSPModel cspModel = modelCreator.get();
		Path p = Paths.get(modelPath);

		final String text;
		try (final Stream<String> lines = Files.lines(p)) {
			text = lines.collect(Collectors.joining("\n"));
		} catch (NoSuchFileException e) {
			// rethrow as FNFE, because the tests expect the old exception
			throw new FileNotFoundException(e.getMessage());
		}

		cspModel = cspModel.create(text, p.toFile());
		return new ExtractedModel<>(cspModel, cspModel.getComponent(p.getFileName().toString()));
	}
}
