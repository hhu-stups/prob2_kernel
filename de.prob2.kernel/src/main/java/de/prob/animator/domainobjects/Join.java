package de.prob.animator.domainobjects;

import de.prob.model.classicalb.ClassicalBModel;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Contains methods for combining {@link IEvalElement}s.
 */
public final class Join {

    private Join() {}

    public static IEvalElement conjunctWithStrings(ClassicalBModel model, List<String> elements) {
        StringJoiner stringJoiner = new StringJoiner(" & ");
        for (String element : elements) {
            stringJoiner.add("(" + element + ")");
        }
        return model.parseFormula(stringJoiner.toString(), FormulaExpand.EXPAND);
    }

    public static IEvalElement conjunct(ClassicalBModel model, List<IEvalElement> elements) {
        return Join.conjunctWithStrings(model, elements.stream().map(IEvalElement::getCode).collect(Collectors.toList()));
    }

    public static IEvalElement disjunctWithStrings(ClassicalBModel model, List<String> elements) {
        StringJoiner stringJoiner = new StringJoiner(" or ");
        for (String element : elements) {
            stringJoiner.add("(" + element + ")");
        }
        return model.parseFormula(stringJoiner.toString(), FormulaExpand.EXPAND);
    }

    public static IEvalElement disjunct(ClassicalBModel model, List<IEvalElement> elements) {
        return Join.disjunctWithStrings(model, elements.stream().map(IEvalElement::getCode).collect(Collectors.toList()));
    }
}
