package de.prob.check.tracereplay.check.refinement;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.statespace.Transition;

import java.util.List;

public class TraceRefinementResult  {

	public final boolean success;
	public final List<Transition> resultTrace;

	public TraceRefinementResult(boolean success, List<Transition> resultTrace) {
		this.success = success;
		this.resultTrace = resultTrace;
	}

	public List<PersistentTransition> getResultTracePersistentTransition()  {
		return PersistentTransition.createFromList(resultTrace);
	}

}
