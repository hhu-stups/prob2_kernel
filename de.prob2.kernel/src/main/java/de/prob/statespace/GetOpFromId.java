package de.prob.statespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.FormulaTranslationMode;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class GetOpFromId extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_op_from_id";
	private final Transition op;
	private static final String PARAMETERS_VARIABLE = "Params";
	private static final String RETURNVALUES_VARIABLE = "RetVals";
	private List<String> params;
	private List<String> returnValues;
	private final EvalOptions options;

	public GetOpFromId(final Transition transition, final EvalOptions options) {
		this.op = transition;
		this.options = options;
	}

	public GetOpFromId(final Transition opInfo, final FormulaExpand expansion) {
		this(opInfo, EvalOptions.DEFAULT.withExpand(expansion)
			// Old code returned ASCII instead of Unicode - keep it that way for compatibility
			.withMode(FormulaTranslationMode.ASCII));
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(op.getId());

		pto.openList();
		this.options.printProlog(pto);
		pto.closeList();

		pto.printVariable(PARAMETERS_VARIABLE);
		pto.printVariable(RETURNVALUES_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		ListPrologTerm plist = BindingGenerator.getList(bindings.get(PARAMETERS_VARIABLE));
		params = Collections.emptyList();
		if (!plist.isEmpty()) {
			params = new ArrayList<>();
		}
		for (PrologTerm p : plist) {
			params.add(p.getFunctor().intern());
		}

		ListPrologTerm rlist = BindingGenerator.getList(bindings.get(RETURNVALUES_VARIABLE));
		returnValues = Collections.emptyList();
		if (!rlist.isEmpty()) {
			returnValues = new ArrayList<>();
		}
		for (PrologTerm r : rlist) {
			returnValues.add(r.getFunctor().intern());
		}

		if (this.options.getMode() == FormulaTranslationMode.ASCII && this.options.getLanguage() == null) {
			// TODO Support caching results for non-default options
			op.setInfo(this.options.getExpand(), params, returnValues);
		}
	}

	/**
	 * @deprecated Use getParameters() instead
	 */
	@Deprecated
	public List<String> getParams() {
		return this.getParameters();
	}

	public List<String> getParameters() {
		return params;
	}

	public List<String> getReturnValues() {
		return returnValues;
	}

	public EvaluatedTransitionInfo getInfo() {
		return new EvaluatedTransitionInfo(this.op, this.getParameters(), this.getReturnValues());
	}
}
