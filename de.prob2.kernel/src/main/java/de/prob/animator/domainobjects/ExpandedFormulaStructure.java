package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

public class ExpandedFormulaStructure {
	private final BVisual2Formula formula;
	private final String label;
	private final String description;
	private final List<BVisual2Formula> subformulas;
	private final List<? extends ExpandedFormulaStructure> children;
	
	ExpandedFormulaStructure(final BVisual2Formula formula, final String label, final String description, final List<BVisual2Formula> subformulas, final List<? extends ExpandedFormulaStructure> children) {
		super();
		this.label = label;
		this.description = description;
		this.formula = formula;
		this.subformulas = subformulas;
		this.children = children;
	}
	
	public static ExpandedFormulaStructure withUnexpandedChildren(final BVisual2Formula formula, final String label, final String description, final List<BVisual2Formula> subformulas) {
		return new ExpandedFormulaStructure(formula, label, description, subformulas, null);
	}
	
	public static ExpandedFormulaStructure withExpandedChildren(final BVisual2Formula formula, final String label, final String description, final List<? extends ExpandedFormulaStructure> children) {
		final List<BVisual2Formula> subformulas = children.stream()
			.map(ExpandedFormulaStructure::getFormula)
			.collect(Collectors.toList());
		return new ExpandedFormulaStructure(formula, label, description, subformulas, children);
	}
	
	public static ExpandedFormulaStructure withoutChildren(final BVisual2Formula formula, final String label, final String description) {
		return withExpandedChildren(formula, label, description, Collections.emptyList());
	}
	
	public static ExpandedFormulaStructure fromPrologTerm(final StateSpace stateSpace, final CompoundPrologTerm cpt) {
		BindingGenerator.getCompoundTerm(cpt, "formula", 4);
		final String label = cpt.getArgument(1).getFunctor();
		final String description = cpt.getArgument(2).getFunctor();
		final BVisual2Formula formula = BVisual2Formula.fromFormulaId(stateSpace, cpt.getArgument(3).getFunctor());
		final List<ExpandedFormulaStructure> children = BindingGenerator.getList(cpt.getArgument(4)).stream()
			.map(pt -> ExpandedFormulaStructure.fromPrologTerm(stateSpace, BindingGenerator.getCompoundTerm(pt, "formula", 4)))
			.collect(Collectors.toList());
		return ExpandedFormulaStructure.withExpandedChildren(formula, label, description, children);
	}
	
	public BVisual2Formula getFormula() {
		return this.formula;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDescription() {
		return description;
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
	 * Get the expanded values of this formula's subformulas. {@code null} is returned if the subformulas have not been expanded, for example when this expanded formula was returned from {@link #withUnexpandedChildren(BVisual2Formula, String, String, List)} or {@link BVisual2Formula#expandNonrecursive(State)}.
	 * 
	 * @return the expanded values of this formula's subformulas, or {@code null} if the subformulas have not been expanded
	 */
	public List<? extends ExpandedFormulaStructure> getChildren() {
		return this.children == null ? null : Collections.unmodifiableList(this.children);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("formula", this.getFormula())
			.add("label", this.getLabel())
			.add("description", this.getDescription())
			.add("subformulas", this.getSubformulas())
			.add("children", this.getChildren())
			.toString();
	}
}
