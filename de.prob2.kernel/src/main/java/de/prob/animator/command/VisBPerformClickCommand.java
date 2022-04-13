package de.prob.animator.command;


import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;


import java.util.ArrayList;
import java.util.List;

public class VisBPerformClickCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_perform_click";
	private static final String TRANS_IDS = "TransIDS";
	private List<String> transIDS;

	private final StateSpace stateSpace;

	private final String svgID;

	private final List<String> metaInfoList;

	private final String stateID;

	public VisBPerformClickCommand(StateSpace stateSpace, String svgID, List<String> metaInfoList, String stateID) {
		this.stateSpace = stateSpace;
		this.svgID = svgID;
		this.metaInfoList = metaInfoList;
		this.stateID = stateID;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(svgID);
		pto.openList();
		for (String metaInfo : metaInfoList) {
			pto.printString(metaInfo);
		}
		pto.closeList();
		pto.printAtomOrNumber(stateID);
		pto.printVariable(TRANS_IDS);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		final PrologTerm resultTerm = bindings.get(TRANS_IDS);
		ListPrologTerm list = (ListPrologTerm) resultTerm;
		List<String> transitions = new ArrayList<>();
		for(PrologTerm id : list) {
			transitions.add(((IntegerPrologTerm) id).getValue().toString());
		}
		this.transIDS = transitions;
	}

	public List<String> getTransIDS() {
		return transIDS;
	}
}
