package de.prob.model.representation;

import com.google.inject.Inject;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.Language;

import java.io.File;

public class SequentProverModel extends XTLModel {

	@Inject
	public SequentProverModel(final StateSpaceProvider ssProvider) {
		super(ssProvider, null);
	}

	public SequentProverModel(final StateSpaceProvider ssProvider, File modelFile) {
		super(ssProvider, modelFile);
	}

	public SequentProverModel create(final File modelFile) {
		return new SequentProverModel(stateSpaceProvider, modelFile);
	}

	@Override
	public Language getLanguage() {
		return Language.SEQUENT_PROVER;
	}

}
