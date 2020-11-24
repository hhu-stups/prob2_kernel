package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

public class ExpandedFormulaStructure {
	public static class Builder {
		BVisual2Formula formula;
		String label;
		String description;
		BVisual2Value value;
		List<BVisual2Formula> subformulas;
		List<? extends ExpandedFormulaStructure> children;
		
		Builder() {
			this.formula = null;
			this.label = null;
			this.description = null;
			this.value = null;
			this.subformulas = null;
			this.children = null;
		}
		
		public ExpandedFormulaStructure.Builder formula(final BVisual2Formula formula) {
			if (this.formula != null) {
				throw new IllegalStateException("formula already set");
			}
			this.formula = formula;
			return this;
		}
		
		public ExpandedFormulaStructure.Builder label(final String label) {
			if (this.label != null) {
				throw new IllegalStateException("label already set");
			}
			this.label = label;
			return this;
		}
		
		public ExpandedFormulaStructure.Builder description(final String description) {
			if (this.description != null) {
				throw new IllegalStateException("description already set");
			}
			this.description = description;
			return this;
		}
		
		public ExpandedFormulaStructure.Builder value(final BVisual2Value value) {
			if (this.value != null) {
				throw new IllegalStateException("value already set");
			}
			this.value = value;
			return this;
		}
		
		public ExpandedFormulaStructure.Builder subformulas(final List<BVisual2Formula> subformulas) {
			if (this.subformulas != null) {
				throw new IllegalStateException("subformulas already set");
			} else if (this.children != null) {
				throw new IllegalStateException("Cannot set both children and subformulas");
			}
			this.subformulas = subformulas;
			return this;
		}
		
		public ExpandedFormulaStructure.Builder children(final List<? extends ExpandedFormulaStructure> children) {
			if (this.children != null) {
				throw new IllegalStateException("children already set");
			} else if (this.subformulas != null) {
				throw new IllegalStateException("Cannot set both subformulas and children");
			}
			this.children = children;
			return this;
		}
		
		public ExpandedFormulaStructure build() {
			if (this.value != null) {
				return new ExpandedFormula(this);
			} else {
				return new ExpandedFormulaStructure(this);
			}
		}
	}
	
	private final BVisual2Formula formula;
	private final String label;
	private final String description;
	private final List<BVisual2Formula> subformulas;
	private final List<? extends ExpandedFormulaStructure> children;
	
	ExpandedFormulaStructure(final ExpandedFormulaStructure.Builder builder) {
		super();
		
		if (builder.formula == null) {
			throw new IllegalArgumentException("Missing required field: formula");
		}
		this.formula = builder.formula;
		
		if (builder.label == null) {
			throw new IllegalArgumentException("Missing required field: label");
		}
		this.label = builder.label;
		
		if (builder.description == null) {
			// Avoid breaking existing code that relies on description never being null
			this.description = "";
		} else {
			this.description = builder.description;
		}
		
		if (builder.children != null) {
			assert builder.subformulas == null;
			this.subformulas = builder.children.stream()
				.map(ExpandedFormulaStructure::getFormula)
				.collect(Collectors.toList());
		} else {
			if (builder.subformulas == null) {
				throw new IllegalArgumentException("Missing required field: subformulas");
			}
			this.subformulas = builder.subformulas;
		}
		
		this.children = builder.children;
	}
	
	public static ExpandedFormulaStructure.Builder builder() {
		return new ExpandedFormulaStructure.Builder();
	}
	
	public static ExpandedFormulaStructure fromPrologTerm(final StateSpace stateSpace, final PrologTerm term) {
		BindingGenerator.getCompoundTerm(term, "formula", 1);
		
		final ExpandedFormulaStructure.Builder builder = builder();
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
					builder.formula(BVisual2Formula.fromFormulaId(stateSpace, arg.getFunctor()));
					if (formula != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					formula = BVisual2Formula.fromFormulaId(stateSpace, arg.getFunctor());
					break;
				
				case "label":
					builder.label(PrologTerm.atomicString(arg));
					if (label != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					label = PrologTerm.atomicString(arg);
					break;
				
				case "description":
					builder.description(PrologTerm.atomicString(arg));
					if (description != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					description = PrologTerm.atomicString(arg);
					break;
				
				case "value":
					builder.value(BVisual2Value.fromPrologTerm(arg));
					if (value != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					value = BVisual2Value.fromPrologTerm(arg);
					break;
				
				case "children_ids":
					builder.subformulas(BindingGenerator.getList(arg).stream()
						.map(id -> BVisual2Formula.fromFormulaId(stateSpace, id.getFunctor()))
						.collect(Collectors.toList()));
					if (unexpandedChildren != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					unexpandedChildren = BindingGenerator.getList(arg).stream()
						.map(id -> BVisual2Formula.fromFormulaId(stateSpace, id.getFunctor()))
						.collect(Collectors.toList());
					break;
				
				case "children":
					builder.children(BindingGenerator.getList(arg).stream()
						.map(childTerm -> ExpandedFormulaStructure.fromPrologTerm(stateSpace, childTerm))
						.collect(Collectors.toList()));
					if (expandedChildren != null) {
						throw new IllegalArgumentException("Duplicate entry: " + entry.getFunctor());
					}
					expandedChildren = BindingGenerator.getList(arg).stream()
						.map(childTerm -> ExpandedFormulaStructure.fromPrologTerm(stateSpace, childTerm))
						.collect(Collectors.toList());
					break;
				
				default:
					// Ignore unknown entries to allow adding more information in the future.
					break;
			}
		}
		
		return builder.build();
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
	 * Get the expanded values of this formula's subformulas. {@code null} is returned if the subformulas have not been expanded, for example when this expanded formula was returned from {@link BVisual2Formula#expandNonrecursive(State)}.
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
