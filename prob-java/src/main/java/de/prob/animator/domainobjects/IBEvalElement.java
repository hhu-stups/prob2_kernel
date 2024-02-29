package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.TranslatingVisitor;

public interface IBEvalElement extends IEvalElement {
	Node getAst();

	@Override
	default String getPrettyPrint() {
		final PrettyPrinter prettyPrinter = new PrettyPrinter();
		this.getAst().apply(prettyPrinter);
		return prettyPrinter.getPrettyPrint();
	}

	default <T extends BValue> T translate() {
		if (!EvalElementType.EXPRESSION.equals(getKind())) {
			throw new IllegalArgumentException("Value translation is only supported for expressions, not " + this.getKind());
		}
		TranslatingVisitor<T> v = new TranslatingVisitor<>();
		getAst().apply(v);
		return v.getResult();
	}
}
