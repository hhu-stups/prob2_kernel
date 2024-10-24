package de.prob.animator.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.prob.animator.domainobjects.VisBClickMetaInfos;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

public class VisBPerformClickCommand extends AbstractCommand implements IStateSpaceModifier {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_perform_click";
	private static final String TRANSITIONS = "Transitions";
	private List<String> transIDS;
	private List<Transition> transitions;

	private final StateSpace stateSpace;
	private final String svgID;
	private final VisBClickMetaInfos metaInfos;
	private final String stateID;

	@Deprecated
	public VisBPerformClickCommand(StateSpace stateSpace, String svgID, List<String> metaInfos, String stateID) {
		this(stateSpace, svgID, new VisBClickMetaInfos(false, false, false, 0, 0,
				false, new HashMap<>()), stateID);
	}

	public VisBPerformClickCommand(StateSpace stateSpace, String svgID, VisBClickMetaInfos metaInfos, String stateID) {
		this.stateSpace = stateSpace;
		this.svgID = svgID;
		this.metaInfos = metaInfos;
		this.stateID = stateID;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(svgID);
		pto.openList();
		metaInfos.printProlog(pto);
		pto.closeList();
		pto.printAtomOrNumber(stateID);
		pto.printVariable(TRANSITIONS);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.transIDS = new ArrayList<>();
		this.transitions = new ArrayList<>();
		for (PrologTerm term : BindingGenerator.getList(bindings, TRANSITIONS)) {
			final Transition trans = Transition.createTransitionFromCompoundPrologTerm(this.stateSpace, BindingGenerator.getCompoundTerm(term, 4));
			this.transitions.add(trans);
			this.transIDS.add(trans.getId());
		}
	}

	/**
	 * @deprecated Use {@link #getTransitions()} instead.
	 */
	@Deprecated
	public List<String> getTransIDS() {
		return transIDS;
	}

	/**
	 * Get the sequence of transitions that should be executed in the current state to perform the click.
	 * 
	 * @return sequence of transitions for performing the click
	 */
	public List<Transition> getTransitions() {
		return this.transitions;
	}

	@Override
	public List<Transition> getNewTransitions() {
		return this.transitions;
	}
}
