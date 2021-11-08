package de.prob.animator.domainobjects;

import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.prob.translator.BValue;

public interface IBEvalElement extends IEvalElement {
	Node getAst();

	@Override
	default String getPrettyPrint() {
		final PrettyPrinter prettyPrinter = new PrettyPrinter();
		this.getAst().apply(prettyPrinter);
		return prettyPrinter.getPrettyPrint();
	}

	<T extends BValue> T translate();
}
