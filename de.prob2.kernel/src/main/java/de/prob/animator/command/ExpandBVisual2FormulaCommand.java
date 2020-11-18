package de.prob.animator.command;

import de.prob.animator.domainobjects.BVisual2Formula;
import de.prob.animator.domainobjects.ExpandedFormulaStructure;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public class ExpandBVisual2FormulaCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_expand_bvisual2_formula";
	
	private static final String EXPANDED_FORMULA_VAR = "ExpandedFormula";
	
	private final BVisual2Formula formula;
	private final State evaluationState;
	private final boolean recursive;
	private ExpandedFormulaStructure expanded;
	
	/**
	 * @param formula the formula to expand
	 * @param evaluationState the state in which to evaluate the expanded formula(s), or {@code null} to only expand and not evaluate
	 * @param recursive whether to recursively expand (and possibly evaluate) the formula's child formulas
	 */
	public ExpandBVisual2FormulaCommand(final BVisual2Formula formula, final State evaluationState, final boolean recursive) {
		this.formula = formula;
		this.evaluationState = evaluationState;
		this.recursive = recursive;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(this.formula.getId());
		pto.openList();
		if (this.evaluationState != null) {
			pto.openTerm("evaluate");
			pto.printAtomOrNumber(this.evaluationState.getId());
			pto.closeTerm();
		}
		if (this.recursive) {
			pto.printAtom("recursive");
		}
		pto.closeList();
		pto.printVariable(EXPANDED_FORMULA_VAR);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.expanded = ExpandedFormulaStructure.fromExtendablePrologTerm(this.formula.getStateSpace(), bindings.get(EXPANDED_FORMULA_VAR));
	}
	
	public ExpandedFormulaStructure getExpanded() {
		return this.expanded;
	}
}
