package de.prob.animator.domainobjects;

import java.util.HashMap;

import de.prob.animator.command.EvaluateFormulaCommand;
import de.prob.statespace.State;

/**
 * @author joy
 * 
 *         Provides uniform methods for {@link IEvalElement}s. Ensures that the
 *         {@link #equals(Object)} and {@link #hashCode()} methods are correctly
 *         implemented (using the value of {@link #code}) so that
 *         {@link HashMap}s work correctly with {@link IEvalElement}s extending
 *         this class.
 */
public abstract class AbstractEvalElement implements IEvalElement {
	private final String code;
	private final FormulaExpand expansion;

	protected AbstractEvalElement(final String code, final FormulaExpand expansion) {
		this.code = code;
		this.expansion = expansion;
	}

	protected AbstractEvalElement(final String code) {
		this(code, FormulaExpand.EXPAND);
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return this.getCode();
	}
	
	@Deprecated
	@Override
	public EvaluateFormulaCommand getCommand(final State state) {
		return new EvaluateFormulaCommand(this, state.getId());
	}
	
	@Override
	public FormulaExpand expansion() {
		return expansion;
	}
}
