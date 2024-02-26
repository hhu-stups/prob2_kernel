package de.prob.statespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
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
	private final EvalOptions options;
	private EvaluatedTransitionInfo info;

	public GetOpFromId(final Transition transition, final EvalOptions options) {
		this.op = transition;
		this.options = options;
	}

	public GetOpFromId(final Transition opInfo, final FormulaExpand expansion) {
		this(opInfo, Transition.OLD_DEFAULT_EVAL_OPTIONS.withExpand(expansion));
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
		// FIXME Is it a good idea to intern *every* parameter/return value? They can be very long if truncation is disabled.

		ListPrologTerm plist = BindingGenerator.getList(bindings.get(PARAMETERS_VARIABLE));
		List<String> params = Collections.emptyList();
		if (!plist.isEmpty()) {
			params = new ArrayList<>();
		}
		for (PrologTerm p : plist) {
			params.add(p.getFunctor().intern());
		}

		ListPrologTerm rlist = BindingGenerator.getList(bindings.get(RETURNVALUES_VARIABLE));
		List<String> returnValues = Collections.emptyList();
		if (!rlist.isEmpty()) {
			returnValues = new ArrayList<>();
		}
		for (PrologTerm r : rlist) {
			returnValues.add(r.getFunctor().intern());
		}

		this.info = new EvaluatedTransitionInfo(this.op, params, returnValues);
		op.addEvaluatedInfo(this.options, this.info);
	}

	/**
	 * @deprecated Use getParameters() instead
	 */
	@Deprecated
	public List<String> getParams() {
		return this.getInfo().getParameterValues();
	}

	public List<String> getParameters() {
		return this.getInfo().getParameterValues();
	}

	public List<String> getReturnValues() {
		return this.getInfo().getReturnValues();
	}

	public EvaluatedTransitionInfo getInfo() {
		return this.info;
	}
}
