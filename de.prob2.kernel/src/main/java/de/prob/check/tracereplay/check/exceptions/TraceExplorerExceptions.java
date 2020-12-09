package de.prob.check.tracereplay.check.exceptions;

import java.util.List;

public class TraceExplorerExceptions extends Exception {

	public TraceExplorerExceptions(List<String> messages){
		super(messages.toString());
	}

	public TraceExplorerExceptions(){
		super();
	}

}
