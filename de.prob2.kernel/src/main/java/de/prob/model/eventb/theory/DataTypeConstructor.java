package de.prob.model.eventb.theory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DataTypeConstructor {
	private final String name;
	private final List<DataTypeDestructor> arguments;
	
	public DataTypeConstructor(final String name, final List<DataTypeDestructor> arguments) {
		this.name = Objects.requireNonNull(name, "name");
		this.arguments = Objects.requireNonNull(arguments, "arguments");
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<DataTypeDestructor> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final DataTypeConstructor other = (DataTypeConstructor)obj;
		return this.getName().equals(other.getName())
			&& this.getArguments().equals(other.getArguments());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName(), this.getArguments());
	}
}
