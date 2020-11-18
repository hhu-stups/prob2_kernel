package de.prob.animator.command;

import de.prob.animator.domainobjects.BVisual2Formula;
import de.prob.animator.domainobjects.ExpandedFormulaStructure;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

/**
 * @deprecated Use {@link ExpandBVisual2FormulaCommand} instead.
 */
@Deprecated
public final class GetBVisual2FormulaStructureCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_get_bvisual2_formula_structure";
	
	private static final String TREE_VARIABLE = "TREE";
	
	private final BVisual2Formula formula;
	
	private ExpandedFormulaStructure result;
	
	public GetBVisual2FormulaStructureCommand(final BVisual2Formula formula) {
		this.formula = formula;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(this.formula.getId());
		pto.printVariable(TREE_VARIABLE);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.result = ExpandedFormulaStructure.fromPrologTerm(this.formula.getStateSpace(), BindingGenerator.getCompoundTerm(bindings.get(TREE_VARIABLE), "formula", 4));
	}
	
	public ExpandedFormulaStructure getResult() {
		return this.result;
	}
}
