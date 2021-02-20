package de.prob.check.tracereplay.check.exceptions;

import de.prob.model.classicalb.Operation;
import de.prob.statespace.OperationInfo;

import java.util.Collections;

public class ToManyOptionsException extends Exception{


	private  OperationInfo oldOperation;
	private  OperationInfo newOperation;
	public ToManyOptionsException(OperationInfo oldOperation, OperationInfo newOperation){
		this.oldOperation = oldOperation;
		this.newOperation = newOperation;
	}

	public ToManyOptionsException(){

	}

	public OperationInfo getOldOperation() {
		return oldOperation;
	}

	public OperationInfo getNewOperation() {
		return newOperation;
	}

}
