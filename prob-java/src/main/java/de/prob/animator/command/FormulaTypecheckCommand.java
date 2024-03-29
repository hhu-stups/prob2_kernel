package de.prob.animator.command;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.animator.domainobjects.TypeCheckResult;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class FormulaTypecheckCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_get_formula_type";
	private static final String TYPE = "Type";
	private static final String ERRORS = "Errors";

	private final IEvalElement formula;
	private TypeCheckResult result;

	public FormulaTypecheckCommand(IEvalElement formula) {
		this.formula = formula;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		formula.printEvalTerm(pto);
		pto.printVariable(TYPE);
		pto.printVariable(ERRORS);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		String type = bindings.get(TYPE).atomToString();
		List<ErrorItem> errors = BindingGenerator.getList(bindings.get(ERRORS)).stream()
			.map(ErrorItem::fromProlog)
			.collect(Collectors.toList());
		result = new TypeCheckResult(type, errors);
	}

	public TypeCheckResult getResult() {
		return result;
	}
}
