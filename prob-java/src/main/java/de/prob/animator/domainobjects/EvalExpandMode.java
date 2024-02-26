package de.prob.animator.domainobjects;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * Controls the expansion of symbolic sets in evaluation results.
 */
public final class EvalExpandMode {
	@SuppressWarnings("InnerClassFieldHidesOuterClassField")
	public enum Mode {
		NEVER,
		LIMIT,
		EFFICIENT,
		FORCE,
	}
	
	/**
	 * Don't expand symbolic sets in the evaluation result.
	 */
	public static final EvalExpandMode NEVER = new EvalExpandMode(EvalExpandMode.Mode.NEVER, -1);
	
	/**
	 * Expand symbolic sets, unless they are known/expected to be inefficient to expand.
	 */
	public static final EvalExpandMode EFFICIENT = new EvalExpandMode(EvalExpandMode.Mode.EFFICIENT, -1);
	
	/**
	 * Expand all symbolic sets without exception, even if doing so may be expensive.
	 */
	public static final EvalExpandMode FORCE = new EvalExpandMode(EvalExpandMode.Mode.FORCE, -1);
	
	private final EvalExpandMode.Mode mode;
	private final int limit;
	
	private EvalExpandMode(final EvalExpandMode.Mode mode, final int limit) {
		this.mode = mode;
		this.limit = limit;
	}
	
	/**
	 * Expand symbolic sets, unless they contain more than the given number of elements
	 * or are otherwise known/expected to be inefficient to expand.
	 * 
	 * @param limit maximum cardinality for expansion
	 * @return expansion mode with the given limit
	 */
	public static EvalExpandMode fromLimit(final int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("Evaluation expansion limit cannot be negative: " + limit);
		}
		return new EvalExpandMode(EvalExpandMode.Mode.LIMIT, limit);
	}
	
	public EvalExpandMode.Mode getMode() {
		return this.mode;
	}
	
	public int getLimit() {
		return this.limit;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final EvalExpandMode other = (EvalExpandMode)obj;
		return this.getLimit() == other.getLimit()
			&& this.getMode() == other.getMode();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getMode(), this.getLimit());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("mode", this.getMode())
			.add("limit", this.getLimit())
			.toString();
	}
	
	public void printProlog(final IPrologTermOutput pout) {
		switch (this.getMode()) {
			case NEVER:
				pout.printAtom("false");
				break;
			
			case LIMIT:
				pout.openTerm("limit");
				pout.printNumber(this.getLimit());
				pout.closeTerm();
				break;
			
			case EFFICIENT:
				pout.printAtom("true");
				break;
			
			case FORCE:
				pout.printAtom("force");
				break;
			
			default:
				throw new AssertionError("Unhandled mode: " + this.getMode());
		}
	}
}
