package de.prob.check.tracereplay.check;

public class ResolvedConflict {
	
	final String oldName;
	final String newName;
	
	
	public ResolvedConflict(String oldName, String newName){
		this.newName = newName;
		this.oldName = oldName;
	}
}
