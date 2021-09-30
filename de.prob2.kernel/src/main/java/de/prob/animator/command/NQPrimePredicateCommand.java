package de.prob.animator.command;

import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * Non-quantifier introducing version of {@link PrimePredicateCommand}.
 *
 * PrimePredicateCommand introduces an existential quantifier around variables
 * which are not part of the machine context
 * and would translate {@code x : INT} to {@code #x'.(x : INT)}. Further,
 * it fails if tasked with priming for instance function members such as
 * {@code x : INT +-> INT} which should be translated to
 * {@code x' : INT +-> INT} by this version.
 */
public class NQPrimePredicateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_nonquantifying_primed_predicate";
	private static final String PRIMED_PREDICATE_VARIABLE = "PrimedPredOut";

	private final IEvalElement evalElement;
	private String result = null;

	public NQPrimePredicateCommand(final IEvalElement evalElement) {
		this.evalElement = evalElement;
	}

	public String getPrimedPredicate() {
		return result;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		CompoundPrologTerm compoundTerm = BindingGenerator.getCompoundTerm(bindings.get(PRIMED_PREDICATE_VARIABLE), 0);
		// Note: It would be more elegant to return an EventB or ClassicalB element, as
		// PrimePredicateCommand attempts.
		// However, the introduced ' as priming symbol hinders the parsing of a ClassicalB element
		// (which is why PrimePredicateCommand doesn't work with ClassicalB input...).
		// We could circumvent it by using Classical B's other apostrophe, ′ (0x007F, not a backtick ´),
		// but for now it makes more sense to let the user handle it themselves.
		result = compoundTerm.getFunctor();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		evalElement.printProlog(pout);
		pout.printVariable(PRIMED_PREDICATE_VARIABLE);
		pout.closeTerm();
	}
}
