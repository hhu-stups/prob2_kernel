package de.prob.scripting;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.prob.model.representation.SequentProverModel;
import de.prob.statespace.StateSpace;

import java.io.File;
import java.io.IOException;

public class SequentProverFactory implements ModelFactory<SequentProverModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<SequentProverModel> modelCreator;

	@Inject
	SequentProverFactory(Provider<StateSpace> stateSpaceProvider, Provider<SequentProverModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<SequentProverModel> extract(final String modelPath) throws IOException {
		return new ExtractedModel<>(stateSpaceProvider, modelCreator.get().create(new File(modelPath)));
	}
}
