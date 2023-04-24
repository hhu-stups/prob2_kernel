package de.prob.model.classicalb;

import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.model.representation.Variable;

public class ClassicalBVariable extends Variable {
	public ClassicalBVariable(final Start start) {
		super(new ClassicalB(start, FormulaExpand.EXPAND));
	}
}
