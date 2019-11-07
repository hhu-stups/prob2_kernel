package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.FormulaId;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetTopLevelFormulasCommand extends AbstractCommand { 
	private static final String PROLOG_COMMAND_NAME = "get_top_level_formulas";
	private static final String TOP_IDS = "TopIds";

	private final List<FormulaId> formulaIds;

	public GetTopLevelFormulasCommand() {
		this.formulaIds = new ArrayList<>();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(TOP_IDS);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		BindingGenerator.getList(bindings, TOP_IDS).stream()
			.map(PrologTerm::atomicString)
			.map(id -> new FormulaId(id, null))
			.collect(Collectors.toCollection(() -> formulaIds));
	}

	public List<FormulaId> getFormulaIds() {
		return Collections.unmodifiableList(formulaIds);
	}
}
