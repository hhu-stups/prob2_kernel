package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.BVisual2Formula;
import de.prob.animator.domainobjects.BVisual2Value;
import de.prob.animator.domainobjects.ExpandedFormula;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

/**
 * @deprecated Use {@link ExpandBVisual2FormulaCommand} instead.
 */
@Deprecated
public class ExpandFormulaNonrecursiveCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "expand_formula_nonrecursive";

	private static final String LABEL_VARIABLE = "Label";
	private static final String DESCRIPTION_VARIABLE = "Description";
	private static final String VALUE_VARIABLE = "Value";
	private static final String SUB_IDS_VARIABLE = "SubIds";

	private final State state;
	private final String id;
	private ExpandedFormula result;

	public ExpandFormulaNonrecursiveCommand(final String id, final State state) {
		this.id = id;
		this.state = state;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(id);
		pto.printAtomOrNumber(state.getId());
		pto.printVariable(LABEL_VARIABLE);
		pto.printVariable(DESCRIPTION_VARIABLE);
		pto.printVariable(VALUE_VARIABLE);
		pto.printVariable(SUB_IDS_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		final BVisual2Formula formula = BVisual2Formula.fromFormulaId(this.state.getStateSpace(), this.id);
		final String name = PrologTerm.atomicString(bindings.get(LABEL_VARIABLE));
		final String description = PrologTerm.atomicString(bindings.get(DESCRIPTION_VARIABLE));
		final BVisual2Value value = BVisual2Value.fromPrologTerm(bindings.get(VALUE_VARIABLE));
		final List<BVisual2Formula> subformulas = BindingGenerator.getList(bindings, SUB_IDS_VARIABLE).stream()
			.map(id -> BVisual2Formula.fromFormulaId(this.state.getStateSpace(), id.getFunctor()))
			.collect(Collectors.toList());
		result = ExpandedFormula.withUnexpandedChildren(formula, name, description, value, subformulas);
	}

	public ExpandedFormula getResult() {
		return result;
	}
}
