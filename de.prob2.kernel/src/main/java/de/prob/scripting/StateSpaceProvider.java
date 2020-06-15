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

	public static void loadFromCommandIntoStateSpace(
		final StateSpace s,
		final AbstractModel model,
		final AbstractElement mainComponent,
		final AbstractCommand loadCmd
	) {
		s.initModel(model, mainComponent);
		s.execute(loadCmd);
		s.execute(new StartAnimationCommand());
	}

	public StateSpace getStateSpace() {
		return this.ssProvider.get();
	}

	public StateSpace loadFromCommand(final AbstractModel model,
			final AbstractElement mainComponent,
			final Map<String, String> preferences, final AbstractCommand loadCmd) {
		StateSpace s = ssProvider.get();

		try {
			s.changePreferences(preferences);
			loadFromCommandIntoStateSpace(s, model, mainComponent, loadCmd);
			return s;
		} catch (RuntimeException e) {
			s.kill();
			throw e;
		}
	}
}
