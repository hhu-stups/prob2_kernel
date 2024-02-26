package de.prob.animator.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class GetErrorItemsCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "get_error_messages_with_span_info";
	private static final String ERRORS_VARIABLE = "Errors";

	private List<ErrorItem> errors = Collections.emptyList();

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printVariable(ERRORS_VARIABLE)
			.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		errors = ((ListPrologTerm)bindings.get(ERRORS_VARIABLE)).stream()
			.map(ErrorItem::fromProlog)
			.collect(Collectors.toList());
	}

	public List<ErrorItem> getErrors() {
		return errors;
	}
}
