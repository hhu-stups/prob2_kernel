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

	public ExtractedModel(Provider<StateSpace> stateSpaceProvider, T model) {
		this.stateSpaceProvider = stateSpaceProvider;
		this.model = model;
	}

	public void loadIntoStateSpace(final StateSpace stateSpace) {
		model.loadIntoStateSpace(stateSpace);
	}

	public StateSpace load() {
		return load(Collections.emptyMap());
	}

	public StateSpace load(final Map<String, String> preferences) {
		StateSpace stateSpace = stateSpaceProvider.get();
		try {
			stateSpace.changePreferences(preferences);
			model.loadIntoStateSpace(stateSpace);
			return stateSpace;
		} catch (RuntimeException e) {
			stateSpace.kill();
			throw e;
		}
	}

	public T getModel() {
		return model;
	}

	/**
	 * This method is no longer needed - you can call {@link AbstractModel#getMainComponent()} directly instead.
	 * 
	 * @return the model's main component
	 */
	public AbstractElement getMainComponent() {
		return this.getModel().getMainComponent();
	}
}
