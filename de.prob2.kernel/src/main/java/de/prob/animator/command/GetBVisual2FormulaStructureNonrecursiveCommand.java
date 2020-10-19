package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.BVisual2Formula;
import de.prob.animator.domainobjects.ExpandedFormulaStructure;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class GetBVisual2FormulaStructureNonrecursiveCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_get_bvisual2_formula_structure_nonrecursive";
	
	private static final String LABEL_VARIABLE = "Label";
	private static final String DESCRIPTION_VARIABLE = "Description";
	private static final String SUB_IDS_VARIABLE = "SubIds";
	
	private final BVisual2Formula formula;
	
	private ExpandedFormulaStructure result;
	
	public GetBVisual2FormulaStructureNonrecursiveCommand(final BVisual2Formula formula) {
		this.formula = formula;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(this.formula.getId());
		pto.printVariable(LABEL_VARIABLE);
		pto.printVariable(DESCRIPTION_VARIABLE);
		pto.printVariable(SUB_IDS_VARIABLE);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		final String name = PrologTerm.atomicString(bindings.get(LABEL_VARIABLE));
		final String description = PrologTerm.atomicString(bindings.get(DESCRIPTION_VARIABLE));
		final List<BVisual2Formula> subformulas = BindingGenerator.getList(bindings, SUB_IDS_VARIABLE).stream()
			.map(id -> BVisual2Formula.fromFormulaId(this.formula.getStateSpace(), id.getFunctor()))
			.collect(Collectors.toList());
		this.result = ExpandedFormulaStructure.withUnexpandedChildren(formula, name, description, subformulas);
	}
	
	public ExpandedFormulaStructure getResult() {
		return this.result;
	}
}
