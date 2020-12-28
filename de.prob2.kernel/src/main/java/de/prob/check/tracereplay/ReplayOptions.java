package de.prob.check.tracereplay;

public class ReplayOptions {
	
	private final boolean checkDestState;
	private final boolean checkDestStateNotChanged;
	private final boolean checkOutput;
	private final boolean enforceAdditionalPredicates;
	private final int lookAhead;
	
	public ReplayOptions(boolean ignoreDestState, boolean ignoreDestStateNotChanged, boolean checkOutput, boolean enforceAdditionalPredicates, int lookAhead){
		this.checkDestState = ignoreDestState;
		this.checkDestStateNotChanged = ignoreDestStateNotChanged;
		this.checkOutput = checkOutput;
		this.enforceAdditionalPredicates = enforceAdditionalPredicates;
		if(lookAhead < 0 ) throw new NumberFormatException("The number should be natural");
		this.lookAhead = lookAhead;
	}
	
	public ReplayOptions(){
		this.checkDestState = true;
		this.checkDestStateNotChanged = true;
		this.checkOutput = false;
		this.enforceAdditionalPredicates = false;
		this.lookAhead = 0;
	}

	public boolean checkDestState() {
		return checkDestState;
	}

	public boolean checkDestStateNotChanged() {
		return checkDestStateNotChanged;
	}

	public boolean checkOutput() {
		return checkOutput;
	}

	public boolean enforceAdditionalPredicates() {
		return enforceAdditionalPredicates;
	}

	public int lookAhead(){
		return lookAhead;
	}
}
