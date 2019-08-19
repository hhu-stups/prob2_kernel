package de.prob.animator.domainobjects;

import java.util.List;
import java.util.StringJoiner;

import de.prob.model.classicalb.ClassicalBModel;
/**
 * Contains methods for combining {@link IEvalElement}s.
 */
public final class Join {

    private Join() {}

    public static IEvalElement conjunct(ClassicalBModel model, List<IEvalElement> elements) {
        StringJoiner stringJoiner = new StringJoiner(" & ");
        for (IEvalElement element : elements) {
            stringJoiner.add("(" + element.getCode() + ")");
        }
        return model.parseFormula(stringJoiner.toString(), FormulaExpand.EXPAND);
    }

    public static IEvalElement disjunct(ClassicalBModel model, List<IEvalElement> elements) {
        StringJoiner stringJoiner = new StringJoiner(" or ");
        for (IEvalElement element : elements) {
            stringJoiner.add("(" + element.getCode() + ")");
        }
        return model.parseFormula(stringJoiner.toString(), FormulaExpand.EXPAND);
    }
}
