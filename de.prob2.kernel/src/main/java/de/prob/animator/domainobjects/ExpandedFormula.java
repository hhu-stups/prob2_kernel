package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

public final class ExpandedFormula {

	public static enum FormulaType {
		EXPRESSION, PREDICATE, OTHER;
		
		public static ExpandedFormula.FormulaType fromProlog(final String prologTypeName) {
			switch (prologTypeName) {
				case "predicate":
					return ExpandedFormula.FormulaType.PREDICATE;
				
				case "expression":
					return ExpandedFormula.FormulaType.EXPRESSION;
				
				default:
					return ExpandedFormula.FormulaType.OTHER;
			}
		}
	}

	public static final class ProofInfo {
		private final int unchangedCount;
		private final int provenCount;
		private final int unprovenCount;
		
		public ProofInfo(final int unchangedCount, final int provenCount, final int unprovenCount) {
			this.unchangedCount = unchangedCount;
			this.provenCount = provenCount;
			this.unprovenCount = unprovenCount;
		}
		
		public static ExpandedFormula.ProofInfo fromPrologTerm(final PrologTerm term) {
			int unchangedCount = 0;
			int provenCount = 0;
			int unprovenCount = 0;
			
			for (final PrologTerm entryTerm : BindingGenerator.getList(term)) {
				final CompoundPrologTerm entry = BindingGenerator.getCompoundTerm(entryTerm, "-", 2);
				switch (PrologTerm.atomicString(entry.getArgument(1))) {
					case "unchanged":
						unchangedCount = BindingGenerator.getInteger(entry.getArgument(2)).getValue().intValue();
						break;
					
					case "proven":
						provenCount = BindingGenerator.getInteger(entry.getArgument(2)).getValue().intValue();
						break;
					
					case "unproven":
						unprovenCount = BindingGenerator.getInteger(entry.getArgument(2)).getValue().intValue();
						break;
				}
			}
			
			return new ExpandedFormula.ProofInfo(unchangedCount, provenCount, unprovenCount);
		}
		
		public int getUnchangedCount() {
			return unchangedCount;
		}
		
		public int getProvenCount() {
			return provenCount;
		}
		
		public int getUnprovenCount() {
			return unprovenCount;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || this.getClass() != obj.getClass()) {
				return false;
			}
			final ExpandedFormula.ProofInfo other = (ExpandedFormula.ProofInfo)obj;
			return this.getUnchangedCount() == other.getUnchangedCount()
				&& this.getProvenCount() == other.getProvenCount()
				&& this.getUnprovenCount() == other.getUnprovenCount();
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.getUnchangedCount(), this.getProvenCount(), this.getUnprovenCount());
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("unchangedCount", unchangedCount)
				.add("provenCount", provenCount)
				.add("unprovenCount", unprovenCount)
				.toString();
		}
	}

	public static class Builder {
		BVisual2Formula formula;
		String label;
		String description;
		ExpandedFormula.ProofInfo proofInfo;
		String functorSymbol;
		List<String> rodinLabels;
		BVisual2Value value;
		FormulaType type;
		List<BVisual2Formula> subformulas;
		List<ExpandedFormula> children;
		
		Builder() {
			this.formula = null;
			this.label = null;
			this.description = null;
			this.proofInfo = null;
			this.value = null;
			this.type = null;
			this.subformulas = null;
			this.children = null;
		}
		
		public ExpandedFormula.Builder formula(final BVisual2Formula formula) {
			if (this.formula != null) {
				throw new IllegalStateException("formula already set");
			}
			this.formula = formula;
			return this;
		}
		
		public ExpandedFormula.Builder label(final String label) {
			if (this.label != null) {
				throw new IllegalStateException("label already set");
			}
			this.label = label;
			return this;
		}
		
		public ExpandedFormula.Builder description(final String description) {
			if (this.description != null) {
				throw new IllegalStateException("description already set");
			}
			this.description = description;
			return this;
		}
		
		public ExpandedFormula.Builder proofInfo(final ExpandedFormula.ProofInfo proofInfo) {
			if (this.proofInfo != null) {
				throw new IllegalStateException("proofInfo already set");
			}
			this.proofInfo = proofInfo;
			return this;
		}
		
		public ExpandedFormula.Builder functorSymbol(final String functorSymbol) {
			if (this.functorSymbol != null) {
				throw new IllegalStateException("functorSymbol already set");
			}
			this.functorSymbol = functorSymbol;
			return this;
		}
		
		public ExpandedFormula.Builder rodinLabels(final List<String> rodinLabels) {
			if (this.rodinLabels != null) {
				throw new IllegalStateException("rodinLabels already set");
			}
			this.rodinLabels = rodinLabels;
			return this;
		}
		
		public ExpandedFormula.Builder value(final BVisual2Value value) {
			if (this.value != null) {
				throw new IllegalStateException("value already set");
			}
			this.value = value;
			return this;
		}

		public ExpandedFormula.Builder type(final ExpandedFormula.FormulaType type) {
			if (this.type != null) {
				throw new IllegalStateException("type already set");
			}
			this.type = type;
			return this;
		}
		
		public ExpandedFormula.Builder subformulas(final List<BVisual2Formula> subformulas) {
			if (this.subformulas != null) {
				throw new IllegalStateException("subformulas already set");
			} else if (this.children != null) {
				throw new IllegalStateException("Cannot set both children and subformulas");
			}
			this.subformulas = subformulas;
			return this;
		}
		
		public ExpandedFormula.Builder children(final List<ExpandedFormula> children) {
			if (this.children != null) {
				throw new IllegalStateException("children already set");
			} else if (this.subformulas != null) {
				throw new IllegalStateException("Cannot set both subformulas and children");
			}
			this.children = children;
			return this;
		}
		
		public ExpandedFormula build() {
			return new ExpandedFormula(this);
		}
	}
	
	private final BVisual2Formula formula;
	private final String label;
	private final String description;
	private final String functorSymbol;
	private final ExpandedFormula.ProofInfo proofInfo;
	private final List<String> rodinLabels;
	private final BVisual2Value value;
	private final FormulaType type;
	private final List<BVisual2Formula> subformulas;
	private final List<ExpandedFormula> children;
	
	ExpandedFormula(final ExpandedFormula.Builder builder) {
		super();
		
		if (builder.formula == null) {
			throw new IllegalArgumentException("Missing required field: formula");
		}
		this.formula = builder.formula;
		
		if (builder.label == null) {
			throw new IllegalArgumentException("Missing required field: label");
		}
		this.label = builder.label;
		
		this.description = builder.description;
		this.proofInfo = builder.proofInfo;
		this.functorSymbol = builder.functorSymbol;
		this.rodinLabels = builder.rodinLabels;
		this.value = builder.value;
		this.type = builder.type;
		
		if (builder.children != null) {
			assert builder.subformulas == null;
			this.subformulas = builder.children.stream()
				.map(ExpandedFormula::getFormula)
				.collect(Collectors.toList());
		} else {
			if (builder.subformulas == null) {
				throw new IllegalArgumentException("Missing required field: subformulas");
			}
			this.subformulas = builder.subformulas;
		}
		
		this.children = builder.children;
	}
	
	public static ExpandedFormula.Builder builder() {
		return new ExpandedFormula.Builder();
	}
	
	public static ExpandedFormula fromPrologTerm(final StateSpace stateSpace, final PrologTerm term) {
		BindingGenerator.getCompoundTerm(term, "formula", 1);
		
		final ExpandedFormula.Builder builder = builder();
		for (final PrologTerm entry : BindingGenerator.getList(term.getArgument(1))) {
			BindingGenerator.getCompoundTerm(entry, 1);
			final PrologTerm arg = entry.getArgument(1);
			switch (entry.getFunctor()) {
				case "id":
					builder.formula(BVisual2Formula.fromFormulaId(stateSpace, arg.getFunctor()));
					break;
				
				case "label":
					builder.label(PrologTerm.atomicString(arg));
					break;
				
				case "description":
					builder.description(PrologTerm.atomicString(arg));
					break;
				
				case "proof_info":
					builder.proofInfo(ExpandedFormula.ProofInfo.fromPrologTerm(arg));
					break;
				
				case "functor_symbol":
					builder.functorSymbol(PrologTerm.atomicString(arg));
					break;
				
				case "rodin_labels":
					builder.rodinLabels(PrologTerm.atomicStrings(BindingGenerator.getList(arg)));
					break;
				
				case "value":
					builder.value(BVisual2Value.fromPrologTerm(arg));
					break;
				case "type":
					builder.type(ExpandedFormula.FormulaType.fromProlog(PrologTerm.atomicString(arg)));
					break;
				case "children_ids":
					builder.subformulas(BindingGenerator.getList(arg).stream()
						.map(id -> BVisual2Formula.fromFormulaId(stateSpace, id.getFunctor()))
						.collect(Collectors.toList()));
					break;
				
				case "children":
					builder.children(BindingGenerator.getList(arg).stream()
						.map(childTerm -> ExpandedFormula.fromPrologTerm(stateSpace, childTerm))
						.collect(Collectors.toList()));
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
	
	public ExpandedFormula.ProofInfo getProofInfo() {
		return this.proofInfo;
	}
	
	public String getFunctorSymbol() {
		return this.functorSymbol;
	}
	
	public List<String> getRodinLabels() {
		return this.rodinLabels == null ? null : Collections.unmodifiableList(this.rodinLabels);
	}
	
	public BVisual2Value getValue() {
		return value;
	}

	public FormulaType getType() {
		return type;
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
	public List<ExpandedFormula> getChildren() {
		return this.children == null ? null : Collections.unmodifiableList(this.children);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("formula", this.getFormula())
			.add("label", this.getLabel())
			.add("description", this.getDescription())
			.add("proofInfo", this.getProofInfo())
			.add("functorSymbol", this.getFunctorSymbol())
			.add("rodinLabels", this.getRodinLabels())
			.add("value", this.getValue())
			.add("type", this.getType())
			.add("subformulas", this.getSubformulas())
			.add("children", this.getChildren())
			.toString();
	}
}
