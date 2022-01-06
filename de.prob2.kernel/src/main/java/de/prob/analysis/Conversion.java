package de.prob.analysis;

import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.representation.AbstractModel;

public class Conversion {

	public static PPredicate predicateFromString(AbstractModel model, String predicateString) {
		Start ast = ((ClassicalB) model.parseFormula(predicateString, FormulaExpand.EXPAND)).getAst();
		return ((APredicateParseUnit) ast.getPParseUnit()).getPredicate();
	}

	public static PPredicate predicateFromClassicalB(ClassicalB classicalB) {
		Start ast = classicalB.getAst();
		return ((APredicateParseUnit) ast.getPParseUnit()).getPredicate();
	}

	public static IEvalElement classicalBFromPredicate(AbstractModel model, PPredicate predicate) {
		PrettyPrinter pp = new PrettyPrinter();
		predicate.apply(pp);
		return model.parseFormula(pp.getPrettyPrint(), FormulaExpand.EXPAND);
	}

	public static PPredicate predicateFromPredicate(AbstractModel model, PPredicate predicate) {
		PrettyPrinter pp = new PrettyPrinter();
		predicate.apply(pp);
		return predicateFromString(model, pp.getPrettyPrint());
	}

}
