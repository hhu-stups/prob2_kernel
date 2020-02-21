package de.prob.animator.domainobjects;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

import de.prob.exception.ProBError;
import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class ExpandedFormula {
	private final String name;
	private final BVisual2Value value;
	private final String id;
	private final List<ExpandedFormula> children;

	public ExpandedFormula(final String name, final BVisual2Value value, final String id, final List<ExpandedFormula> children) {
		this.name = name;
		this.value = value;
		this.id = id;
		this.children = children;
	}

	public static ExpandedFormula fromPrologTerm(final CompoundPrologTerm cpt) {
		BindingGenerator.getCompoundTerm(cpt, "formula", 4);
		final String name = cpt.getArgument(1).getFunctor();
		final BVisual2Value result = getValue(name, cpt.getArgument(2));
		final String id = cpt.getArgument(3).getFunctor();
		final List<ExpandedFormula> children = BindingGenerator.getList(cpt.getArgument(4)).stream()
			.map(pt -> ExpandedFormula.fromPrologTerm(BindingGenerator.getCompoundTerm(pt, "formula", 4)))
			.collect(Collectors.toList());
		return new ExpandedFormula(name, result, id, children);
	}

	private static BVisual2Value getValue(final String name, final PrologTerm term) {
		final String functor = term.getFunctor();
		switch (functor) {
			case "p":
				BindingGenerator.getCompoundTerm(term, "p", 1);
				final String value = PrologTerm.atomicString(term.getArgument(1));
				switch (value) {
					case "false":
						return BVisual2Value.PredicateValue.FALSE;
					
					case "true":
						return BVisual2Value.PredicateValue.TRUE;
					
					default:
						throw new ProBError("Invalid value in predicate result: " + value);
				}
			
			case "v":
				BindingGenerator.getCompoundTerm(term, "v", 1);
				return new BVisual2Value.ExpressionValue(PrologTerm.atomicString(term.getArgument(1)));
			
			case "e":
				BindingGenerator.getCompoundTerm(term, "e", 1);
				return new BVisual2Value.Error(PrologTerm.atomicString(term.getArgument(1)));
			
			case "i":
				BindingGenerator.getCompoundTerm(term, "i", 0);
				return BVisual2Value.Inactive.INSTANCE;
			
			default:
				throw new ProBError("Unhandled expanded formula value type: " + term);
		}
	}

	public String getLabel() {
		return name;
	}

	public BVisual2Value getValue() {
		return value;
	}

	public List<ExpandedFormula> getChildren() {
		return children;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", this.getLabel())
			.add("value", this.getValue())
			.add("id", this.getId())
			.add("children", this.getChildren())
			.toString();
	}
}
