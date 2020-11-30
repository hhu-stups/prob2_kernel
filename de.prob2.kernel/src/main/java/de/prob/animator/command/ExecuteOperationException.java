package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

public class ExecuteOperationException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	private List<GetOperationByPredicateCommand.GetOperationError> errors;

	public ExecuteOperationException(String msg, List<GetOperationByPredicateCommand.GetOperationError> errors) {
		super(msg);
		this.errors = errors;
	}

	public List<GetOperationByPredicateCommand.GetOperationError> getErrors() {
		return errors;
	}

	public List<String> getErrorMessages() {
		return errors.stream().map(GetOperationByPredicateCommand.GetOperationError::getMessage).collect(Collectors.toList());
	}
}
