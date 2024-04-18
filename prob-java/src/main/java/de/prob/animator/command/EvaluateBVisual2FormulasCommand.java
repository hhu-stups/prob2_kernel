package de.prob.animator.command;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.BVisual2Formula;
import de.prob.animator.domainobjects.BVisual2Value;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class EvaluateBVisual2FormulasCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_evaluate_bvisual2_formulas";
	
	private static final String VALUES_VARIABLE = "Values";
	
	private final Collection<BVisual2Formula> formulas;
	private final State state;
	private final boolean unlimited;
	
	private List<BVisual2Value> results;
	
	public EvaluateBVisual2FormulasCommand(final Collection<BVisual2Formula> formulas, final State state) {
		this(formulas, state, false);
	}

	public EvaluateBVisual2FormulasCommand(final Collection<BVisual2Formula> formulas, final State state, final boolean unlimited) {
		this.formulas = formulas;
		this.state = state;
		this.unlimited = unlimited;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.openList();
		this.formulas.stream()
			.map(BVisual2Formula::getId)
			.forEachOrdered(pto::printAtomOrNumber);
		pto.closeList();
		pto.printAtomOrNumber(this.state.getId());
		pto.openList();
		if (this.unlimited) {
			pto.printAtom("unlimited");
		}
		pto.closeList();
		pto.printVariable(VALUES_VARIABLE);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.results = BindingGenerator.getList(bindings, VALUES_VARIABLE)
			.stream()
			.map(BVisual2Value::fromPrologTerm)
			.collect(Collectors.toList());
	}
	
	public List<BVisual2Value> getResults() {
		return this.results;
	}
}
