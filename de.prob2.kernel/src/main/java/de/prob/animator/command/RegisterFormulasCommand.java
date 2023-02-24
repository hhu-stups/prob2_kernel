package de.prob.animator.command;

import java.util.Collection;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class RegisterFormulasCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "register_prob2_formulas";
	private final Collection<? extends IEvalElement> formulas;

	public RegisterFormulasCommand(final Collection<? extends IEvalElement> formulas) {
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
		pto.openList();
		for (final IEvalElement formula : this.formulas) {
			pto.openTerm("eval");
			formula.printProlog(pto);
			pto.printAtom(formula.getKind().getPrologName());
			pto.printAtom(formula.expansion().getPrologName());
			pto.closeTerm();
		}
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
