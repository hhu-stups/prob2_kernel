package de.prob.animator.domainobjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

/**
 * The VisBEvent is designed for the JSON / VisB file
 */
public class VisBHover {
	private final String svgID; // id of the object whose attribute is modified upon hover
	private final String hoverID;
	private final String hoverAttribute;
	private final String hoverEnterValue;
	private final String hoverLeaveValue;

	public VisBHover(String svgID, String hoverID, String hoverAttribute, String hoverEnterValue, String hoverLeaveValue){
		this.svgID = svgID;
		this.hoverID = hoverID;
		this.hoverAttribute = hoverAttribute;
		this.hoverEnterValue = hoverEnterValue;
		this.hoverLeaveValue = hoverLeaveValue;
	}

	public String getSVGID() {
		return svgID;
	}

	public String getHoverID() {
		return hoverID;
	}

	public String getHoverAttr() {
		return hoverAttribute;
	}
	public String getHoverEnterVal() {
		return hoverEnterValue;
	}
	public String getHoverLeaveVal() {
		return hoverLeaveValue;
	}

	public static VisBHover fromPrologTerm(final PrologTerm term) {
		BindingGenerator.getCompoundTerm(term, "hover", 5);
		final String svgID = term.getArgument(1).atomToString();
		final String hoverID = term.getArgument(2).atomToString();
		final String attribute = term.getArgument(3).atomToString();
		final String enterVal = term.getArgument(4).atomToString();
		final String leaveVal = term.getArgument(5).atomToString();
		return new VisBHover(svgID, hoverID, attribute, enterVal, leaveVal);
	}

	@Override
	public String toString(){
		return "<Hover changing " + hoverID + "." + hoverAttribute+ " upon enter: "+hoverEnterValue+ " leave: " + hoverLeaveValue + ">";
	}
}
