package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.ZModel;

public class ZFactory implements ModelFactory<ZModel> {
	private final Provider<ZModel> modelCreator;
	
	@Inject
	public ZFactory(final Provider<ZModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<ZModel> extract(final String modelPath) throws IOException {
		final File f = new File(modelPath);
		final ZModel zModel = modelCreator.get().create(f);
		return new ExtractedModel<>(zModel, null);
	}
}
