package de.prob.animator.command;

import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class EnsureWdCommand extends AbstractCommand {
	private String result = null;
	private static final String PROLOG_COMMAND_NAME = "prob2_ensure_wd";

	private final IEvalElement evalElement;
	private static final String RESULT_VARIABLE = "WdPred";

	public EnsureWdCommand(IEvalElement evalElement) {
		this.evalElement = evalElement;
	}


	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		CompoundPrologTerm compoundTerm = BindingGenerator.getCompoundTerm(bindings.get(RESULT_VARIABLE), 0);

		result = compoundTerm.getFunctor();
		if (evalElement instanceof EventB) {
			// probcli returns Unicode primes (Classical B syntax), which the Rodin parser doesn't support
			result = result.replace('′', '\'');
		}

	}

	@Override
	public void writeCommand(IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		evalElement.printProlog(pout);
		pout.printVariable(RESULT_VARIABLE);
		pout.closeTerm();
	}

	public String getWdPred() {
		return result;
	}
}
