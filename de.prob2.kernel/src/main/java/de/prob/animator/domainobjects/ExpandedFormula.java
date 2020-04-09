package de.prob.animator.domainobjects;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.statespace.StateSpace;

public class ExpandedFormula {
	private final String label;
	private final BVisual2Value value;
	private final BVisual2Formula formula;
	private final List<ExpandedFormula> children;

	public ExpandedFormula(final String label, final BVisual2Value value, final BVisual2Formula formula, final List<ExpandedFormula> children) {
		this.label = label;
		this.value = value;
		this.formula = formula;
		this.children = children;
	}

	public static ExpandedFormula fromPrologTerm(final StateSpace stateSpace, final CompoundPrologTerm cpt) {
		BindingGenerator.getCompoundTerm(cpt, "formula", 4);
		final String label = cpt.getArgument(1).getFunctor();
		final BVisual2Value result = BVisual2Value.fromPrologTerm(cpt.getArgument(2));
		final BVisual2Formula formula = BVisual2Formula.fromFormulaId(stateSpace, cpt.getArgument(3).getFunctor());
		final List<ExpandedFormula> children = BindingGenerator.getList(cpt.getArgument(4)).stream()
			.map(pt -> ExpandedFormula.fromPrologTerm(stateSpace, BindingGenerator.getCompoundTerm(pt, "formula", 4)))
			.collect(Collectors.toList());
		return new ExpandedFormula(label, result, formula, children);
	}

	public String getLabel() {
		return label;
	}

	public BVisual2Value getValue() {
		return value;
	}

	public List<ExpandedFormula> getChildren() {
		return children;
	}

	public BVisual2Formula getFormula() {
		return this.formula;
	}
	
	/**
	 * @deprecated Use {@link #getFormula()} instead.
	 */
	@Deprecated
	public String getId() {
		return this.getFormula().getId();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("label", this.getLabel())
			.add("value", this.getValue())
			.add("formula", this.getFormula())
			.add("children", this.getChildren())
			.toString();
	}
}
