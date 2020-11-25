package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.GetOpFromId;
import de.prob.statespace.Transition;

public class GetOpsFromIds extends AbstractCommand {
	private final ComposedCommand allCommands;

	public GetOpsFromIds(final Collection<Transition> edges, final FormulaExpand expansion) {
		List<GetOpFromId> opInfos = new ArrayList<>();

		//copy edges to avoid race condition
		List<Transition> copiedEdges = new ArrayList<>(edges);
		for(Transition opInfo : copiedEdges) {
			if(opInfo.canBeEvaluated(expansion)) {
				opInfos.add(new GetOpFromId(opInfo, expansion));
			}
		}
		allCommands = new ComposedCommand(opInfos);
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
