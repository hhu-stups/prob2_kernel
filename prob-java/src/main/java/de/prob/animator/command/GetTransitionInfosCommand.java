package de.prob.animator.command;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetTransitionInfosCommand extends AbstractCommand {

	// prob2_get_transition_infos(TransId,Infos)
	private static final String PROLOG_COMMAND_NAME = "prob2_get_transition_infos";
	private static final String RESULT_VARIABLE = "Infos";

	private final String transitionId;
	private final Map<String, List<PrologTerm>> infos = new HashMap<>();

	public GetTransitionInfosCommand(final String transitionId) {
		this.transitionId = transitionId;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		if (bindings.get(RESULT_VARIABLE).isList()) {
			for (PrologTerm term : BindingGenerator.getList(bindings.get(RESULT_VARIABLE))) {
				// collect all infos with the same functor
				List<PrologTerm> args = new ArrayList<>();
				for (int i = 1; i <= term.getArity(); i++) {
					args.add(term.getArgument(i));
				}
				infos.put(term.getFunctor(), args);
			}
		}
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(transitionId);
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	public Map<String,List<PrologTerm>> getInfos() {
		return infos;
	}
}
