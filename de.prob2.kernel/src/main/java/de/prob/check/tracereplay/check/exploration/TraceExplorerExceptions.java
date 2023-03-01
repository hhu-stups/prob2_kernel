package de.prob.check.tracereplay.check.exploration;

import java.util.List;
@Deprecated
public class TraceExplorerExceptions extends Exception {
	private static final long serialVersionUID = 1L;

	@Deprecated
	public TraceExplorerExceptions(List<String> messages){
		super(messages.toString());
	}

	@Deprecated
	public TraceExplorerExceptions(){
		super();
	}

}
