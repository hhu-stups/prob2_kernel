package de.prob.animator.domainobjects;

import java.util.Collection;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.prob.animator.command.EvaluateFormulaCommand;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;

/**
 * Wrapper for another {@link IEvalElement} that has been registered with ProB
 * and can now be evaluated more efficiently.
 * 
 * @see StateSpace#getRegisteredFormulas()
 * @see StateSpace#registerFormulas(Collection)
 * @see StateSpace#unregisterFormulas(Collection)
 */
public final class RegisteredFormula implements IEvalElement {
	private final IEvalElement formula;
	
	/**
	 * Do not call this constructor directly.
	 * Use {@link StateSpace#registerFormulas(Collection)} to register a formula
	 * and {@link StateSpace#getRegisteredFormulas()} to get the corresponding {@link RegisteredFormula} object.
	 * 
	 * @param formula the formula to be wrapped, which must already be registered on the Prolog side
	 */
	public RegisteredFormula(final IEvalElement formula) {
		this.formula = formula;
	}
	
	public IEvalElement getFormula() {
		return this.formula;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final RegisteredFormula other = (RegisteredFormula)obj;
		return this.getFormula().equals(other.getFormula());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getFormula());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("formulaId", this.getFormulaId())
			.add("formula", this.getFormula())
			.toString();
	}
	
	@Override
	public String getCode() {
		return this.getFormula().getCode();
	}
	
	@Override
	public String getPrettyPrint() {
		return this.getFormula().getPrettyPrint();
	}
	
	@Override
	public void printProlog(final IPrologTermOutput pout) {
		throw new UnsupportedOperationException("Registered formulas cannot be printed in old eval term format");
	}
	
	@Override
	public void printEvalTerm(final IPrologTermOutput pout) {
		pout.openTerm("registered");
		this.getFormulaId().printUUID(pout);
		pout.closeTerm();
	}
	
	@Override
	public EvalElementType getKind() {
		return this.getFormula().getKind();
	}
	
	@Deprecated
	@Override
	public String serialized() {
		return this.getFormula().serialized();
	}
	
	@Override
	public IFormulaUUID getFormulaId() {
		return this.getFormula().getFormulaId();
	}
	
	@Deprecated
	@Override
	public EvaluateFormulaCommand getCommand(final State state) {
		throw new UnsupportedOperationException("Registered formulas cannot be evaluated using EvaluateFormulaCommand");
	}
	
	@Override
	public FormulaExpand expansion() {
		return this.getFormula().expansion();
	}
}
