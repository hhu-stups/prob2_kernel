package de.prob.animator.domainobjects;

import java.util.Objects;

import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.prob.translator.BValue;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.tla2b.exceptions.ExpressionTranslationException;
import de.tla2bAst.Translator;

import util.ToolIO;

public final class TLA extends AbstractEvalElement implements IBEvalElement {

	private Start cachedAST;
	private ClassicalB cachedFormula;

	public TLA(String code) {
		this(code, FormulaExpand.EXPAND);
	}

	public TLA(String code, FormulaExpand expand) {
		super(Objects.requireNonNull(code, "code"), expand);
	}

	private static Start fromTLA(String code) {
		ToolIO.setMode(ToolIO.TOOL);
		Start start;
		try {
			start = Translator.translateTlaExpression(code);
			return start;
		} catch (ExpressionTranslationException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public void printProlog(IPrologTermOutput pout) {
		this.getFormula().printProlog(pout);
	}

	@Override
	public EvalElementType getKind() {
		return this.getFormula().getKind();
	}

	@Override
	public IFormulaUUID getFormulaId() {
		return this.getFormula().getFormulaId();
	}

	@Override
	@Deprecated
	public <T extends BValue> T translate() {
		return this.getFormula().translate();
	}

	@Override
	public Start getAst() {
		if (this.cachedAST == null) {
			this.cachedAST = fromTLA(this.getCode());
		}
		return this.cachedAST;
	}

	private ClassicalB getFormula() {
		if (this.cachedFormula == null) {
			this.cachedFormula = new ClassicalB(this.getAst(), this.expansion());
		}
		return this.cachedFormula;
	}
}
