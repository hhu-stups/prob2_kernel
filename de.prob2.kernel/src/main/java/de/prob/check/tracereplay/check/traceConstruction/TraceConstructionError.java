package de.prob.check.tracereplay.check.traceConstruction;

import java.util.List;

public class TraceConstructionError extends Exception{

	final List<String> errors;
	public TraceConstructionError(List<String> errors){
		this.errors = errors;
	}
}
