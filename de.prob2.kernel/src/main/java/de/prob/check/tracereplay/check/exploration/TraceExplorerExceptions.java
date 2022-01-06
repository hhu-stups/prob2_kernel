package de.prob.check.tracereplay.check.exploration;

import java.util.List;

public class TraceExplorerExceptions extends Exception {
	private static final long serialVersionUID = 1L;

	public TraceExplorerExceptions(List<String> messages){
		super(messages.toString());
	}

	public TraceExplorerExceptions(){
		super();
	}

}
