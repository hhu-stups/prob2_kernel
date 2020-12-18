package de.prob.animator.command;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

	/**
	 * @deprecated There are now more types than just errors and warnings. Use {@code errors.stream().map(ErrorItem::getType).max(ErrorItem.Type::compareTo)} to get the worst type of error item from a list.
	 */
	@Deprecated
	public boolean onlyWarningsOccurred() {
		final Optional<ErrorItem.Type> worstErrorType = this.getErrors().stream()
			.map(ErrorItem::getType)
			.max(ErrorItem.Type::compareTo);
		return !worstErrorType.isPresent() || worstErrorType.get().compareTo(ErrorItem.Type.WARNING) <= 0;
	}

	public List<ErrorItem> getErrors() {
		return errors;
	}
}
