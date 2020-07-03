package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.ZFuzzModel;
import de.prob.model.representation.ZModel;

public class ZFuzzFactory implements ModelFactory<ZModel> {
	private final Provider<ZFuzzModel> modelCreator;
	
	@Inject
	public ZFuzzFactory(final Provider<ZFuzzModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<ZModel> extract(final String modelPath) throws IOException, ModelTranslationError {
		final File f = new File(modelPath);
		final ZFuzzModel zModel = modelCreator.get().create(f);
		return new ExtractedModel<>(zModel, null);
	}
}
