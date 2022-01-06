package de.prob.animator.domainobjects;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import de.prob.formula.PredicateBuilder;
import de.prob.model.classicalb.ClassicalBModel;
import de.prob.model.representation.AbstractModel;

// TODO The purpose of this class is similar to PredicateBuilder - should they be merged?
/**
 * Contains methods for combining {@link IEvalElement}s.
 * 
 * @see PredicateBuilder
 */
public final class Join {

	private Join() {}

	public static IEvalElement conjunctWithStrings(AbstractModel model, List<String> elements) {
		final PredicateBuilder pb = new PredicateBuilder();
		for (String element : elements) {
			pb.add("(" + element + ")");
		}
		return model.parseFormula(pb.toString(), FormulaExpand.EXPAND);
	}

	public static IEvalElement conjunct(AbstractModel model, List<IEvalElement> elements) {
		return Join.conjunctWithStrings(model, elements.stream().map(IEvalElement::getCode).collect(Collectors.toList()));
	}

	public static IEvalElement disjunctWithStrings(AbstractModel model, List<String> elements) {
		StringJoiner stringJoiner = new StringJoiner(" or ");
		for (String element : elements) {
			stringJoiner.add("(" + element + ")");
		}
		return model.parseFormula(stringJoiner.toString(), FormulaExpand.EXPAND);
	}

	public static IEvalElement disjunct(AbstractModel model, List<IEvalElement> elements) {
		return Join.disjunctWithStrings(model, elements.stream().map(IEvalElement::getCode).collect(Collectors.toList()));
	}
}
