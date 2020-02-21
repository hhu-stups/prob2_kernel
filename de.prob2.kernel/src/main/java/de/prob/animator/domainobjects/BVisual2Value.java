package de.prob.animator.domainobjects;

import java.util.Objects;

public interface BVisual2Value {
	enum PredicateValue implements BVisual2Value {
		FALSE(false),
		TRUE(true),
		;
		
		private final boolean value;
		
		PredicateValue(final boolean value) {
			this.value = value;
		}
		
		public boolean getValue() {
			return this.value;
		}
		
		@Override
		public String toString() {
			return String.valueOf(this.getValue());
		}
	}
	
	final class ExpressionValue implements BVisual2Value {
		private final String value;
		
		public ExpressionValue(final String value) {
			this.value = Objects.requireNonNull(value, "value");
		}
		
		public String getValue() {
			return this.value;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || this.getClass() != obj.getClass()) {
				return false;
			}
			final BVisual2Value.ExpressionValue other = (BVisual2Value.ExpressionValue)obj;
			return this.getValue().equals(other.getValue());
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.getValue());
		}
		
		@Override
		public String toString() {
			return this.getValue();
		}
	}
	
	final class Error implements BVisual2Value {
		private final String message;
		
		public Error(final String message) {
			this.message = Objects.requireNonNull(message, "message");
		}
		
		public String getMessage() {
			return this.message;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || this.getClass() != obj.getClass()) {
				return false;
			}
			final BVisual2Value.Error other = (BVisual2Value.Error)obj;
			return this.getMessage().equals(other.getMessage());
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.getMessage());
		}
		
		@Override
		public String toString() {
			return String.format("(error: %s)", this.getMessage());
		}
	}
	
	enum Inactive implements BVisual2Value {
		INSTANCE;
		
		@Override
		public String toString() {
			return "(inactive)";
		}
	}
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(final Object obj);
	
	@Override
	String toString();
}
