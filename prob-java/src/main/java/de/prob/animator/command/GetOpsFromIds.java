package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.GetOpFromId;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

/**
 * @deprecated Use {@link StateSpace#evaluateTransitions(Collection, EvalOptions)} instead, which provides a high-level API for what this command did.
 *     If you really need to use commands, use individual {@link GetOpFromId} commands and execute them together.
 */
@Deprecated
public class GetOpsFromIds extends AbstractCommand {
	private final ComposedCommand allCommands;

	public GetOpsFromIds(final Collection<Transition> edges, final EvalOptions options) {
		List<GetOpFromId> opInfos = new ArrayList<>();
		for(Transition opInfo : edges) {
			if(!opInfo.isEvaluated(options)) {
				opInfos.add(new GetOpFromId(opInfo, options));
			}
		}
		allCommands = new ComposedCommand(opInfos);
	}

	public GetOpsFromIds(final Collection<Transition> edges, final FormulaExpand expansion) {
		this(edges, Transition.OLD_DEFAULT_EVAL_OPTIONS.withExpand(expansion));
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		allCommands.writeCommand(pto);
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		allCommands.processResult(bindings);
	}

	@Override
	public List<AbstractCommand> getSubcommands() {
		return allCommands.getSubcommands();
	}
}
