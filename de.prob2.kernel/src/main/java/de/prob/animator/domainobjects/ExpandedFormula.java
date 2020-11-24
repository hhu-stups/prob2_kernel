package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.statespace.StateSpace;

public final class ExpandedFormula extends ExpandedFormulaStructure {
	private final BVisual2Value value;

	ExpandedFormula(final ExpandedFormulaStructure.Builder builder) {
		super(builder);

		if (builder.value == null) {
			throw new IllegalArgumentException("Missing required field: value");
		}
		this.value = builder.value;
	}

	/**
	 * @deprecated Use {@link ExpandedFormulaStructure#builder()} instead.
	 */
	@Deprecated
	public static ExpandedFormula withUnexpandedChildren(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value, final List<BVisual2Formula> subformulas) {
		return (ExpandedFormula)ExpandedFormulaStructure.builder()
			.formula(formula)
			.label(label)
			.description(description)
			.value(value)
			.subformulas(subformulas)
			.build();
	}

	/**
	 * @deprecated Use {@link ExpandedFormulaStructure#builder()} instead.
	 */
	@Deprecated
	public static ExpandedFormula withUnexpandedChildren(final BVisual2Formula formula, final String label, final BVisual2Value value, final List<BVisual2Formula> subformulas) {
		return withUnexpandedChildren(formula, label, "", value, subformulas);
	}

	/**
	 * @deprecated Use {@link ExpandedFormulaStructure#builder()} instead.
	 */
	@Deprecated
	public static ExpandedFormula withExpandedChildren(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value, final List<ExpandedFormula> children) {
		return (ExpandedFormula)ExpandedFormulaStructure.builder()
			.formula(formula)
			.label(label)
			.description(description)
			.value(value)
			.children(children)
			.build();
	}

	/**
	 * @deprecated Use {@link ExpandedFormulaStructure#builder()} instead.
	 */
	@Deprecated
	public static ExpandedFormula withExpandedChildren(final BVisual2Formula formula, final String label, final BVisual2Value value, final List<ExpandedFormula> children) {
		return withExpandedChildren(formula, label, "", value, children);
	}

	/**
	 * @deprecated Use {@link ExpandedFormulaStructure#builder()} instead.
	 */
	@Deprecated
	public static ExpandedFormula withoutChildren(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value) {
		return withExpandedChildren(formula, label, description, value, Collections.emptyList());
	}

	/**
	 * @deprecated Use {@link ExpandedFormulaStructure#builder()} instead.
	 */
	@Deprecated
	public static ExpandedFormula withoutChildren(final BVisual2Formula formula, final String label, final BVisual2Value value) {
		return withoutChildren(formula, label, "", value);
	}

	public static ExpandedFormula fromPrologTerm(final StateSpace stateSpace, final CompoundPrologTerm cpt) {
		BindingGenerator.getCompoundTerm(cpt, "formula", 5);
		return (ExpandedFormula)ExpandedFormulaStructure.builder()
			.label(cpt.getArgument(1).getFunctor())
			.description(cpt.getArgument(2).getFunctor())
			.value(BVisual2Value.fromPrologTerm(cpt.getArgument(3)))
			.formula(BVisual2Formula.fromFormulaId(stateSpace, cpt.getArgument(4).getFunctor()))
			.children(BindingGenerator.getList(cpt.getArgument(5)).stream()
				.map(pt -> ExpandedFormula.fromPrologTerm(stateSpace, BindingGenerator.getCompoundTerm(pt, "formula", 5)))
				.collect(Collectors.toList()))
			.build();
	}

	@Override
	public List<ExpandedFormula> getChildren() {
		// This is safe - the ExpandedFormula constructor requires that children is a List<ExpandedFormula>.
		@SuppressWarnings("unchecked")
		final List<ExpandedFormula> children = (List<ExpandedFormula>)super.getChildren();
		return children;
	}

	public BVisual2Value getValue() {
		return value;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("label", this.getLabel())
			.add("description", this.getDescription())
			.add("value", this.getValue())
			.add("formula", this.getFormula())
			.add("subformulas", this.getSubformulas())
			.add("children", this.getChildren())
			.toString();
	}
}
