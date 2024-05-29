package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.ZModel;
import de.prob.statespace.StateSpace;

public class ZFactory implements ModelFactory<ZModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<ZModel> modelCreator;
	
	@Inject
	ZFactory(Provider<StateSpace> stateSpaceProvider, Provider<ZModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<ZModel> extract(final String modelPath) throws IOException {
		final File f = new File(modelPath);
		final ZModel zModel = modelCreator.get().create(f);
		return new ExtractedModel<>(stateSpaceProvider, zModel);
	}
}
