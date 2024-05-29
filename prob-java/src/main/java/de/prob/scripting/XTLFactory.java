package de.prob.scripting;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.model.representation.XTLModel;
import de.prob.statespace.StateSpace;

public class XTLFactory implements ModelFactory<XTLModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final Provider<XTLModel> modelCreator;
	
	@Inject
	XTLFactory(Provider<StateSpace> stateSpaceProvider, Provider<XTLModel> modelCreator) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.modelCreator = modelCreator;
	}

	@Override
	public ExtractedModel<XTLModel> extract(final String modelPath) throws IOException {
		final File f = new File(modelPath);
		final XTLModel xtlModel = modelCreator.get().create(f);
		return new ExtractedModel<>(stateSpaceProvider, xtlModel, null);
	}
}
