package de.prob.model.eventb;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadEventBFileCommand;
import de.prob.model.representation.AbstractElement;
import de.prob.scripting.StateSpaceProvider;

public final class EventBPackageModel extends EventBModel {
	private final String loadCommandPrologCode;
	
	@Inject
	public EventBPackageModel(final StateSpaceProvider stateSpaceProvider) {
		this(stateSpaceProvider, null);
	}
	
	private EventBPackageModel(final StateSpaceProvider stateSpaceProvider, final String loadCommandPrologCode) {
		super(stateSpaceProvider);
		
		this.loadCommandPrologCode = loadCommandPrologCode;
	}
	
	public String getLoadCommandPrologCode() {
		return this.loadCommandPrologCode;
	}
	
	public EventBPackageModel setLoadCommandPrologCode(final String loadCommandPrologCode) {
		return new EventBPackageModel(this.stateSpaceProvider, loadCommandPrologCode);
	}
	
	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		if (this.getLoadCommandPrologCode() == null) {
			throw new IllegalStateException("loadCommandPrologCode must be set before loading an EventBPackageModel");
		}

		return new LoadEventBFileCommand(this.getLoadCommandPrologCode());
	}
}
