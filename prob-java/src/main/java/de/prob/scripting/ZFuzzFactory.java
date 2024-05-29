package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.ZFuzzModel;
import de.prob.model.representation.ZModel;
import de.prob.statespace.StateSpace;

public class ZFuzzFactory implements ModelFactory<ZModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<ZFuzzModel> modelCreator;
	
	@Inject
	ZFuzzFactory(Provider<StateSpace> stateSpaceProvider, Provider<ZFuzzModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<ZModel> extract(final String modelPath) throws IOException {
		final File f = new File(modelPath);
		final ZFuzzModel zModel = modelCreator.get().create(f);
		return new ExtractedModel<>(stateSpaceProvider, zModel);
	}
}
