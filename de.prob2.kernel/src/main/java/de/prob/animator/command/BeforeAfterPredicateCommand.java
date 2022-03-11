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
import de.prob.statespace.OperationInfo;

public class BeforeAfterPredicateCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "before_after_predicate";
	private static final String BA_PRED_VARIABLE = "BAPredicate";

	private final String operationName;
	private final OperationInfo.Type operationType;
	private IEvalElement result = null;

	public BeforeAfterPredicateCommand(final String operationName) {
		// Default operation type is EventB due to historical reasons.
		// Initially, only EventB constructs where returned. By using EventB as default,
		// we do not break any code dependent on this command.
		this(operationName, OperationInfo.Type.EVENTB);
	}

	public BeforeAfterPredicateCommand(
			final String operationName,
			final OperationInfo.Type operationType) {
		this.operationName = operationName;
		this.operationType = operationType;
	}

	public IEvalElement getBeforeAfterPredicate() {
		return result;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		CompoundPrologTerm compoundTerm = BindingGenerator.getCompoundTerm(bindings.get(BA_PRED_VARIABLE), 0);
		String code = compoundTerm.getFunctor();
		if (operationType.equals(OperationInfo.Type.EVENTB)) {
			// probcli returns Unicode primes (Classical B syntax), which the Rodin parser doesn't support
			result = new EventB(code.replace('′', '\''), FormulaExpand.EXPAND);
		} else {
			result = new ClassicalB(code, FormulaExpand.EXPAND);
		}
	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		pout.printAtom(operationName);
		pout.printVariable(BA_PRED_VARIABLE);
		pout.closeTerm();
	}
}
