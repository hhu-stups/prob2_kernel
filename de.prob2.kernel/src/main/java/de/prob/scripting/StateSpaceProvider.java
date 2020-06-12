package de.prob.scripting;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.StartAnimationCommand;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.statespace.StateSpace;

public class StateSpaceProvider {
	private final Provider<StateSpace> ssProvider;

	@Inject
	public StateSpaceProvider(final Provider<StateSpace> ssProvider) {
		this.ssProvider = ssProvider;
	}

	public StateSpace loadFromCommand(final AbstractModel model,
			final AbstractElement mainComponent,
			final Map<String, String> preferences, final AbstractCommand loadCmd) {
		StateSpace s = ssProvider.get();
		s.setModel(model, mainComponent);

		try {
			s.changePreferences(preferences);
			s.execute(loadCmd);
			s.execute(new StartAnimationCommand());
		} catch (RuntimeException e) {
			s.kill();
			throw e;
		}
		return s;
	}
}
