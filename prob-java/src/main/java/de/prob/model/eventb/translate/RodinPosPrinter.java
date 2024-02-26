package de.prob.model.eventb.translate;

import java.util.HashMap;
import java.util.Map;

import de.be4.classicalb.core.parser.analysis.prolog.PositionPrinter;
import de.be4.classicalb.core.parser.node.Node;
import de.prob.prolog.output.IPrologTermOutput;

public class RodinPosPrinter implements PositionPrinter {

	private IPrologTermOutput pout;
	private final Map<Node, RodinPosition> positions = new HashMap<>();

	public void addPositions(final Map<Node, RodinPosition> positions) {
		this.positions.putAll(positions);
	}

	@Override
	public void setPrologTermOutput(final IPrologTermOutput pout) {
		this.pout = pout;
	}

	@Override
	public void printPosition(final Node node) {
		final RodinPosition pos = positions.get(node);
		if (pos == null) {
			pout.printAtom("none");
		} else {
			pout.openTerm("rodinpos");
			pout.printAtom(pos.getModelName());
			pout.printAtom(pos.getLabel());
			pout.emptyList();
			pout.closeTerm();
		}

	}

}
