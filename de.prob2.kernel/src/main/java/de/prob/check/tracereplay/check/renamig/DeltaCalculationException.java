package de.prob.check.tracereplay.check.renamig;

public class DeltaCalculationException extends Exception{
	private static final long serialVersionUID = 1L;

	Exception exception;
	public DeltaCalculationException(Exception e){
		exception = e;
	}
}
