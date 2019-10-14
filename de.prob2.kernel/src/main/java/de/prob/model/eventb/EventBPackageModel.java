package de.prob.model.eventb;

import java.util.Map;

import com.google.inject.Inject;

import de.prob.animator.command.LoadEventBFileCommand;
import de.prob.model.representation.AbstractElement;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.StateSpace;

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
		return new EventBPackageModel(this.getStateSpaceProvider(), loadCommandPrologCode);
	}
	
	@Override
	public StateSpace load(final AbstractElement mainComponent, final Map<String, String> preferences) {
		if (this.getLoadCommandPrologCode() == null) {
			throw new IllegalStateException("loadCommandPrologCode must be set before loading an EventBPackageModel");
		}
		
		return getStateSpaceProvider().loadFromCommand(this, mainComponent, preferences, new LoadEventBFileCommand(this.getLoadCommandPrologCode()));
	}
}
