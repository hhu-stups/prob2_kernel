package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetConstantsPredicateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_constants_predicate";
	private static final String PREDICATE = "Predicate";

	private String constantsPredicate;

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(PREDICATE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		constantsPredicate = bindings.get(PREDICATE).atomToString().replace("\n","");
	}

	public String getPredicate() {
		return constantsPredicate;
	}

}
