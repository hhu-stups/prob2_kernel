package de.prob.animator.domainobjects;

import java.util.Objects;

import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.prob.translator.BValue;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.tla2b.exceptions.ExpressionTranslationException;
import de.tla2b.exceptions.TLA2BException;
import de.tla2bAst.Translator;

import util.ToolIO;

public final class TLA extends AbstractEvalElement implements IBEvalElement {
	private final Start ast;
	private final ClassicalB formula;

	public TLA(String code) {
		this(code, FormulaExpand.EXPAND);
	}

	public TLA(String code, FormulaExpand expand) {
		this(code, expand, null);
	}

	public TLA(String code, FormulaExpand expand, Translator translator) {
		super(Objects.requireNonNull(code, "code"), expand);

		this.ast = fromTLA(code, translator);
		this.formula = new ClassicalB(ast, expand);
	}

	private static Start fromTLA(String code, Translator translator) {
		ToolIO.setMode(ToolIO.TOOL);
		try {
			if (translator != null) {
				// evaluate expression in module context if available
				return translator.translateExpressionIncludingModel(code);
			} else {
				return Translator.translateExpressionWithoutModel(code);
			}
		} catch (ExpressionTranslationException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public void printProlog(IPrologTermOutput pout) {
		this.asClassicalB().printProlog(pout);
	}

	@Override
	public EvalElementType getKind() {
		return this.asClassicalB().getKind();
	}

	@Override
	public IFormulaUUID getFormulaId() {
		return this.asClassicalB().getFormulaId();
	}

	@Override
	public <T extends BValue> T translate() {
		return this.asClassicalB().translate();
	}

	@Override
	public Start getAst() {
		return this.ast;
	}

	private ClassicalB asClassicalB() {
		return this.formula;
	}
}
