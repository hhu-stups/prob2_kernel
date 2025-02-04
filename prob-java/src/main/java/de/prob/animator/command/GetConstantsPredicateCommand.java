package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetConstantsPredicateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_constants_predicate";
	private static final String PREDICATE = "Predicate";
	private static final String PREDICATE_COMPLETE = "PredicateComplete";

	private String constantsPredicate;
	private boolean predicateComplete;

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(PREDICATE);
		pto.printVariable(PREDICATE_COMPLETE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		constantsPredicate = bindings.get(PREDICATE).atomToString().replace("\n      ","");
		predicateComplete = bindings.get(PREDICATE_COMPLETE).atomToString().equals("complete"); // can be 'incomplete' or 'complete'
	}

	public String getPredicate() {
		return constantsPredicate;
	}

	public boolean getPredicateComplete() {
		return predicateComplete;
	}

}
