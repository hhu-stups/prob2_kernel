package de.prob.check.tracereplay;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public class PersistentTrace {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String description = "";
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<PersistentTransition> transitionList = new ArrayList<>();

	public PersistentTrace(Trace trace) {
		this(trace, trace.getTransitionList().size());
	}

	public PersistentTrace(Trace trace, int count) {
		if (count < 1) {
			return;
		}
		List<Transition> list = trace.getTransitionList();
		transitionList.add(new PersistentTransition(list.get(count - 1), true, null));
		for (int i = count - 2; i >= 0; i--) {
			PersistentTransition trans = new PersistentTransition(list.get(i), true, transitionList.get(0));
			transitionList.add(0, trans);
		}
	}

	/**
	 * Jackson constructor
	 *
	 * @param description    the description of the trace
	 * @param transitionList the transition kist
	 */
	@JsonCreator
	public PersistentTrace(
		@JsonProperty("description") String description,
		@JsonProperty("transitionList") List<PersistentTransition> transitionList
	) {
		this.description = description != null ? description : "";
		if (transitionList != null) {
			this.transitionList.addAll(transitionList);
		}
	}

	public List<PersistentTransition> getTransitionList() {
		return this.transitionList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description != null ? description : "";
	}

	public PersistentTrace setTrace(List<PersistentTransition> trace) {
		return new PersistentTrace(this.description, trace);
	}

	public PersistentTrace changeTrace(List<PersistentTransition> trace) {
		return new PersistentTrace(description, trace);
	}
}
