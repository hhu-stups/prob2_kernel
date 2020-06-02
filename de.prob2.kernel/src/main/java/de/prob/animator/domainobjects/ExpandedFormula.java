package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

public class ExpandedFormula {
	private final String label;
	private final String description;
	private final BVisual2Value value;
	private final BVisual2Formula formula;
	private final List<BVisual2Formula> subformulas;
	private final List<ExpandedFormula> children;

	private ExpandedFormula(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value, final List<BVisual2Formula> subformulas, final List<ExpandedFormula> children) {
		this.label = label;
		this.description = description;
		this.value = value;
		this.formula = formula;
		this.subformulas = subformulas;
		this.children = children;
	}

	public static ExpandedFormula withUnexpandedChildren(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value, final List<BVisual2Formula> subformulas) {
		return new ExpandedFormula(formula, label, description, value, subformulas, null);
	}

	/**
	 * @deprecated Use {@link #withUnexpandedChildren(BVisual2Formula, String, String, BVisual2Value, List)} (with an added description parameter) instead.
	 */
	@Deprecated
	public static ExpandedFormula withUnexpandedChildren(final BVisual2Formula formula, final String label, final BVisual2Value value, final List<BVisual2Formula> subformulas) {
		return withUnexpandedChildren(formula, label, "", value, subformulas);
	}

	public static ExpandedFormula withExpandedChildren(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value, final List<ExpandedFormula> children) {
		final List<BVisual2Formula> subformulas = children.stream()
			.map(ExpandedFormula::getFormula)
			.collect(Collectors.toList());
		return new ExpandedFormula(formula, label, description, value, subformulas, children);
	}

	/**
	 * @deprecated Use {@link #withExpandedChildren(BVisual2Formula, String, String, BVisual2Value, List)} (with an added description parameter) instead.
	 */
	@Deprecated
	public static ExpandedFormula withExpandedChildren(final BVisual2Formula formula, final String label, final BVisual2Value value, final List<ExpandedFormula> children) {
		return withExpandedChildren(formula, label, "", value, children);
	}

	public static ExpandedFormula withoutChildren(final BVisual2Formula formula, final String label, final String description, final BVisual2Value value) {
		return withExpandedChildren(formula, label, description, value, Collections.emptyList());
	}

	/**
	 * @deprecated Use {@link #withoutChildren(BVisual2Formula, String, String, BVisual2Value)} (with an added description parameter) instead.
	 */
	@Deprecated
	public static ExpandedFormula withoutChildren(final BVisual2Formula formula, final String label, final BVisual2Value value) {
		return withoutChildren(formula, label, "", value);
	}

	public static ExpandedFormula fromPrologTerm(final StateSpace stateSpace, final CompoundPrologTerm cpt) {
		BindingGenerator.getCompoundTerm(cpt, "formula", 4);
		final String label = cpt.getArgument(1).getFunctor();
		final String description = cpt.getArgument(2).getFunctor();
		final BVisual2Value result = BVisual2Value.fromPrologTerm(cpt.getArgument(3));
		final BVisual2Formula formula = BVisual2Formula.fromFormulaId(stateSpace, cpt.getArgument(4).getFunctor());
		final List<ExpandedFormula> children = BindingGenerator.getList(cpt.getArgument(5)).stream()
			.map(pt -> ExpandedFormula.fromPrologTerm(stateSpace, BindingGenerator.getCompoundTerm(pt, "formula", 5)))
			.collect(Collectors.toList());
		return ExpandedFormula.withExpandedChildren(formula, label, description, result, children);
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public BVisual2Value getValue() {
		return value;
	}

	/**
	 * Get the subformulas of this formula. Unlike {@link #getChildren()}, the formulas are returned as unevaluated {@link BVisual2Formula} objects.
	 * 
	 * @return the subformulas of this formula
	 */
	public List<BVisual2Formula> getSubformulas() {
		return Collections.unmodifiableList(this.subformulas);
	}
	
	/**
	 * Get the expanded values of this formula's subformulas. {@code null} is returned if the subformulas have not been expanded, for example when this expanded formula was returned from {@link #withUnexpandedChildren(BVisual2Formula, String, String, BVisual2Value, List)} or {@link BVisual2Formula#expandNonrecursive(State)}.
	 * 
	 * @return the expanded values of this formula's subformulas, or {@code null} if the subformulas have not been expanded
	 */
	public List<ExpandedFormula> getChildren() {
		return this.children == null ? null : Collections.unmodifiableList(this.children);
	}

	public BVisual2Formula getFormula() {
		return this.formula;
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
