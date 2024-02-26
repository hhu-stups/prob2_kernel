package de.prob.model.eventb.translate;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class RodinPosition {
	private final String modelName;
	private final String label;
	
	public RodinPosition(final String modelName, final String label) {
		this.modelName = modelName;
		this.label = label;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final RodinPosition other = (RodinPosition)obj;
		return this.getModelName().equals(other.getModelName())
			&& this.getLabel().equals(other.getLabel());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getModelName(), this.getLabel());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("modelName", this.getModelName())
			.add("label", this.getLabel())
			.toString();
	}
}
