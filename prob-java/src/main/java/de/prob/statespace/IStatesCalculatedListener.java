package de.prob.statespace;

import java.util.List;

public interface IStatesCalculatedListener {
	void newTransitions(List<Transition> newOps);
}
