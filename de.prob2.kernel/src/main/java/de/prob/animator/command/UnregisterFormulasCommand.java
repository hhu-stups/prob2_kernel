package de.prob.animator.command;

import java.util.List;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class UnregisterFormulasCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "unregister_prob2_formulas";
	private final List<IEvalElement> formulas;

	public UnregisterFormulasCommand(final List<IEvalElement> formulas) {
		this.formulas = formulas;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.openList();
		for (final IEvalElement formula : this.formulas) {
			formula.getFormulaId().printUUID(pto);
		}
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}

}
