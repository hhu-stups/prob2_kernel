package de.prob.animator.domainobjects;

import java.util.List;

import com.google.common.base.MoreObjects;

public final class ExpandedFormula extends ExpandedFormulaStructure {
	private final BVisual2Value value;

	ExpandedFormula(final ExpandedFormulaStructure.Builder builder) {
		super(builder);

		if (builder.value == null) {
			throw new IllegalArgumentException("Missing required field: value");
		}
		this.value = builder.value;
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
