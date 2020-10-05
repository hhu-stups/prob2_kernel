package de.prob.check.tracereplay.check;

public class Conflict {
	
	final Type type;
	final String oldName;
	
	public Conflict(Type type, String oldName){
		this.type = type;
		this.oldName = oldName;
	}
	
	enum Type{
		Renamed, Deleted, Added, NoConflict
	}
}
