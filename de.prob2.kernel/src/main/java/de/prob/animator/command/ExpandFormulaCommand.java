package de.prob.animator.command;

import de.prob.animator.domainobjects.BVisual2Formula;
import de.prob.animator.domainobjects.ExpandedFormula;
import de.prob.animator.domainobjects.FormulaId;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public class ExpandFormulaCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "expand_formula";
	private static final String TREE = "TREE";

	private final State stateId;
	private final String id;
	private ExpandedFormula result;

	public ExpandFormulaCommand(final String id, final State stateId) {
		this.id = id;
		this.stateId = stateId;
	}
	
	/**
	 * @deprecated Use {@link ExpandFormulaCommand#ExpandFormulaCommand(String, State)}, or use {@link BVisual2Formula#expand(State)} instead of this command.
	 */
	@Deprecated
	public ExpandFormulaCommand(final FormulaId id, final State stateId) {
		this.id = id.getId();
		this.stateId = stateId;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(id);
		pto.printAtomOrNumber(stateId.getId());
		pto.printVariable(TREE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		result = ExpandedFormula.fromPrologTerm(this.stateId.getStateSpace(), BindingGenerator.getCompoundTerm(bindings.get(TREE), "formula", 4));
	}

	public ExpandedFormula getResult() {
		return result;
	}
}
