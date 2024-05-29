package de.prob.model.representation;

import java.io.File;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadZFuzzCommand;
import de.prob.scripting.StateSpaceProvider;

public class ZFuzzModel extends ZModel {
	@Inject
	public ZFuzzModel(final StateSpaceProvider ssProvider) {
		this(ssProvider, null);
	}

	public ZFuzzModel(final StateSpaceProvider ssProvider, File modelFile) {
		super(ssProvider, modelFile);
	}

	@Override
	public ZFuzzModel create(final File modelFile) {
		return new ZFuzzModel(stateSpaceProvider, modelFile);
	}
	
	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadZFuzzCommand(this.getModelFile().getAbsolutePath());
	}
}
