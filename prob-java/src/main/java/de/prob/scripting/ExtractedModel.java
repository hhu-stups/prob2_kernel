package de.prob.scripting;

import java.util.Collections;
import java.util.Map;

import com.google.inject.Provider;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.statespace.StateSpace;

public class ExtractedModel<T extends AbstractModel> {
	private final Provider<StateSpace> stateSpaceProvider;
	private final T model;
	private final AbstractElement mainComponent;

	public ExtractedModel(Provider<StateSpace> stateSpaceProvider, T model, AbstractElement mainComponent) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.model = model;
		this.mainComponent = mainComponent;
	}

	public void loadIntoStateSpace(final StateSpace stateSpace) {
		model.loadIntoStateSpace(stateSpace, mainComponent);
	}

	public StateSpace load() {
		return load(Collections.emptyMap());
	}

	public StateSpace load(final Map<String, String> preferences) {
		StateSpace stateSpace = stateSpaceProvider.get();
		try {
			stateSpace.changePreferences(preferences);
			model.loadIntoStateSpace(stateSpace, mainComponent);
			return stateSpace;
		} catch (RuntimeException e) {
			stateSpace.kill();
			throw e;
		}
	}

	public T getModel() {
		return model;
	}

	public AbstractElement getMainComponent() {
		return mainComponent;
	}
}
