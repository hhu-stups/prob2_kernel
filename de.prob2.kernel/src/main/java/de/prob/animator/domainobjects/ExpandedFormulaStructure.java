package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
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
	
	public static ExpandedFormulaStructure fromExtendablePrologTerm(final StateSpace stateSpace, final PrologTerm term) {
		BindingGenerator.getCompoundTerm(term, "formula", 1);
		
		BVisual2Formula formula = null;
		String label = null;
		String description = null;
		BVisual2Value value = null;
		List<BVisual2Formula> unexpandedChildren = null;
		List<ExpandedFormulaStructure> expandedChildren = null;
		for (final PrologTerm entry : BindingGenerator.getList(term.getArgument(1))) {
			BindingGenerator.getCompoundTerm(entry, 1);
			final PrologTerm arg = entry.getArgument(1);
			switch (entry.getFunctor()) {
				case "id":
					if (formula != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					formula = BVisual2Formula.fromFormulaId(stateSpace, arg.getFunctor());
					break;
				
				case "label":
					if (label != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					label = PrologTerm.atomicString(arg);
					break;
				
				case "description":
					if (description != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					description = PrologTerm.atomicString(arg);
					break;
				
				case "value":
					if (value != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					value = BVisual2Value.fromPrologTerm(arg);
					break;
				
				case "children_ids":
					if (unexpandedChildren != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					unexpandedChildren = BindingGenerator.getList(arg).stream()
						.map(id -> BVisual2Formula.fromFormulaId(stateSpace, id.getFunctor()))
						.collect(Collectors.toList());
					break;
				
				case "children":
					if (expandedChildren != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					expandedChildren = BindingGenerator.getList(arg).stream()
						.map(childTerm -> ExpandedFormulaStructure.fromExtendablePrologTerm(stateSpace, childTerm))
						.collect(Collectors.toList());
					break;
				
				default:
					// Ignore unknown entries to allow adding more information in the future.
					break;
			}
		}
		
		if (formula == null) {
			throw new IllegalArgumentException("Missing formula ID");
		} else if (label == null) {
			throw new IllegalArgumentException("Missing label");
		}
		
		if (description == null) {
			// Avoid breaking existing code that relies on description never being null
			description = "";
		}
		
		if (unexpandedChildren != null) {
			if (value == null) {
				return ExpandedFormulaStructure.withUnexpandedChildren(formula, label, description, unexpandedChildren);
			} else {
				return ExpandedFormula.withUnexpandedChildren(formula, label, description, value, unexpandedChildren);
			}
		} else if (expandedChildren != null) {
			if (value == null) {
				return ExpandedFormulaStructure.withExpandedChildren(formula, label, description, expandedChildren);
			} else {
				// ExpandedFormula expects all of its children to also be evaluated.
				final List<ExpandedFormula> expandedChildrenCast = expandedChildren.stream()
					.map(ExpandedFormula.class::cast)
					.collect(Collectors.toList());
				return ExpandedFormula.withExpandedChildren(formula, label, description, value, expandedChildrenCast);
			}
		} else {
			throw new IllegalArgumentException("Either children_ids or children must be present in entries");
		}
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
