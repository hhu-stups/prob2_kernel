package de.prob.statespace;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class EvaluatedTransitionInfo {
	private final Transition transition;
	private final List<String> parameterValues;
	private final List<String> returnValues;
	
	public EvaluatedTransitionInfo(final Transition transition, final List<String> parameterValues, final List<String> returnValues) {
		this.transition = transition;
		this.parameterValues = parameterValues;
		this.returnValues = returnValues;
	}
	
	public Transition getTransition() {
		return this.transition;
	}
	
	public List<String> getParameterValues() {
		return Collections.unmodifiableList(parameterValues);
	}
	
	public List<String> getReturnValues() {
		return Collections.unmodifiableList(returnValues);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final EvaluatedTransitionInfo other = (EvaluatedTransitionInfo)obj;
		return this.getTransition().equals(other.getTransition())
			&& this.getParameterValues().equals(other.getParameterValues())
			&& this.getReturnValues().equals(other.getReturnValues());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getTransition(), this.getParameterValues(), this.getReturnValues());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("transition", this.getTransition())
			.add("parameterValues", this.getParameterValues())
			.add("returnValues", this.getReturnValues())
			.toString();
	}
}
