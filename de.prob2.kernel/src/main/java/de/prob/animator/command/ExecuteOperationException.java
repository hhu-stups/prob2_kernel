package de.prob.animator.command;

import java.util.List;

public class ExecuteOperationException extends IllegalArgumentException {

    private List<GetOperationByPredicateCommand.GetOperationError> errors;

    public ExecuteOperationException(String msg, List<GetOperationByPredicateCommand.GetOperationError> errors) {
        super(msg);
        this.errors = errors;
    }

    public List<GetOperationByPredicateCommand.GetOperationError> getErrors() {
        return errors;
    }
}