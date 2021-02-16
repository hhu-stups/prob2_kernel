package de.prob.check.tracereplay.check.exceptions;

public class DeltaCalculationException extends Exception{

	Exception exception;
	public DeltaCalculationException(Exception e){
		exception = e;
	}
}
