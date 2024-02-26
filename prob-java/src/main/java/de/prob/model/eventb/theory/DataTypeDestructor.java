package de.prob.model.eventb.theory;

import java.util.Objects;

public final class DataTypeDestructor {
	private final String name;
	private final String type;
	
	public DataTypeDestructor(final String name, final String type) {
		this.name = Objects.requireNonNull(name, "name");
		this.type = Objects.requireNonNull(type, "type");
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final DataTypeDestructor other = (DataTypeDestructor)obj;
		return this.getName().equals(other.getName())
			&& this.getType().equals(other.getType());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName(), this.getType());
	}
}
