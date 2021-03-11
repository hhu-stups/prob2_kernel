package de.prob.check.tracereplay.check.renamig;

public class DeltaCalculationException extends Exception{

	Exception exception;
	public DeltaCalculationException(Exception e){
		exception = e;
	}
}
