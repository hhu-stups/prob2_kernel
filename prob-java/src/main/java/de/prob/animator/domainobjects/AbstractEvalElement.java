package de.prob.animator.domainobjects;

import java.util.Objects;

/**
 * @author joy
 * <p>
 * Provides uniform methods for {@link IEvalElement}s. Ensures that the
 * {@link #equals(Object)} and {@link #hashCode()} methods are correctly
 * implemented (using the value of {@link #code}) so that
 * {@link java.util.HashMap}s work correctly with {@link IEvalElement}s extending
 * this class.
 * </p>
 */
public abstract class AbstractEvalElement implements IEvalElement {

	private final String code;
	/**
	 * This is obsolete.
	 *
	 * @see IEvalElement#expansion()
	 */
	private final FormulaExpand expansion;

	protected AbstractEvalElement(final String code, final FormulaExpand expansion) {
		this.code = code; // code might be null - for example in ClassicalB it is computed lazily
		this.expansion = Objects.requireNonNull(expansion, "expansion");
	}

	protected AbstractEvalElement(final String code) {
		this(code, FormulaExpand.EXPAND);
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public FormulaExpand expansion() {
		return this.expansion;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o == null || this.getClass() != o.getClass()) {
			return false;
		} else {
			AbstractEvalElement that = (AbstractEvalElement) o;
			return this.getCode().equals(that.getCode()) && this.expansion().equals(that.expansion());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getCode(), this.expansion());
	}

	@Override
	public String toString() {
		return this.getCode();
	}
}
