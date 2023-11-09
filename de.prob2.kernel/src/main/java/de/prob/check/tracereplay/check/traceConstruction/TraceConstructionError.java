package de.prob.check.tracereplay.check.traceConstruction;

import de.prob.statespace.Transition;

import java.util.List;

public class TraceConstructionError extends Exception{
	private static final long serialVersionUID = 1L;

	final List<String> errors;
	final List<Transition> trace;
	public TraceConstructionError(List<String> errors, List<Transition> trace){
		this.errors = errors;
		this.trace = trace;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();

		builder.append("Trace Construction failed due to:");
		builder.append("\n");
		errors.forEach(entry -> builder.append(entry).append("\n"));

		return builder.toString();
	}
}
