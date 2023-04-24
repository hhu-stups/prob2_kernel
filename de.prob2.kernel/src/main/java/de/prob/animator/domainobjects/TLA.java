package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.prob.translator.BValue;
import de.prob.model.representation.IFormulaUUID;
import de.prob.prolog.output.IPrologTermOutput;
import de.tla2b.exceptions.ExpressionTranslationException;
import de.tla2bAst.Translator;

import util.ToolIO;

public class TLA extends AbstractEvalElement implements IBEvalElement {

	private final Start ast;
	private final ClassicalB classicalB;

	public TLA(String code) {
		this(code, FormulaExpand.EXPAND);
	}

	public TLA(String code, FormulaExpand expand) {
		super(code, expand);
		ast = fromTLA(code);
		classicalB = new ClassicalB(ast, expand);
	}

	private Start fromTLA(String code) {
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
		classicalB.printProlog(pout);
	}

	@Override
	public EvalElementType getKind() {
		return classicalB.getKind();
	}

	@Deprecated
	@Override
	public String serialized() {
		throw new UnsupportedOperationException("TLA formulas cannot be serialized");
	}

	@Override
	public IFormulaUUID getFormulaId() {
		return classicalB.getFormulaId();
	}

	@Override
	public FormulaExpand expansion() {
		return classicalB.expansion();
	}

	@Override
	public <T extends BValue> T translate() {
		return classicalB.translate();
	}

	@Override
	public Node getAst() {
		return ast;
	}

}
