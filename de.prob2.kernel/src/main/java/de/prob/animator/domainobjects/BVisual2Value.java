package de.prob.animator.domainobjects;

import java.util.Objects;

import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

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
	
	public static BVisual2Value fromPrologTerm(final PrologTerm term) {
		final String functor = term.getFunctor();
		switch (functor) {
			case "p":
				BindingGenerator.getCompoundTerm(term, "p", 1);
				final String value = term.getArgument(1).atomToString();
				switch (value) {
					case "false":
						return BVisual2Value.PredicateValue.FALSE;
					
					case "true":
						return BVisual2Value.PredicateValue.TRUE;
					
					default:
						throw new ProBError("Invalid value in predicate result: " + value);
				}
			
			case "v":
				BindingGenerator.getCompoundTerm(term, "v", 1);
				return new BVisual2Value.ExpressionValue(term.getArgument(1).atomToString());
			
			case "e":
				BindingGenerator.getCompoundTerm(term, "e", 1);
				return new BVisual2Value.Error(term.getArgument(1).atomToString());
			
			case "i":
				BindingGenerator.getCompoundTerm(term, "i", 0);
				return BVisual2Value.Inactive.INSTANCE;
			
			default:
				throw new ProBError("Unhandled expanded formula value type: " + term);
		}
	}
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(final Object obj);
	
	@Override
	String toString();
}
