package de.prob.animator.command;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.RegisteredFormula;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class RegisterFormulasCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "register_prob2_formulas";
	private final Collection<? extends IEvalElement> formulas;
	private final Map<IEvalElement, RegisteredFormula> registered;

	public RegisterFormulasCommand(final Collection<? extends IEvalElement> formulas) {
		this.formulas = formulas;
		this.registered = new LinkedHashMap<>();
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
		// Currently, every formula has an UUID automatically assigned by the Java side.
		// This could be changed in the future to make the Prolog side assign IDs only once the formulas are actually registered.
		for (final IEvalElement formula : this.formulas) {
			this.registered.put(formula, new RegisteredFormula(formula));
		}
	}

	public Map<IEvalElement, RegisteredFormula> getRegistered() {
		return Collections.unmodifiableMap(this.registered);
	}
}
