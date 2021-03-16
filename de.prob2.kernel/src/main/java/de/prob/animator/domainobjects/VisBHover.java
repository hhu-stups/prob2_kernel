package de.prob.animator.domainobjects;


import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

import java.util.List;

/**
 * The VisBEvent is designed for the JSON / VisB file
 */
public class VisBHover {
	private String svgID; // id of the object whose attribute is modified upon hover
	private String hoverID;
	private String hoverAttribute;
	private String hoverEnterValue;
	private String hoverLeaveValue;

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
		final String svgID = PrologTerm.atomicString(term.getArgument(1));
		final String hoverID = PrologTerm.atomicString(term.getArgument(2));
		final String attribute = PrologTerm.atomicString(term.getArgument(3));
		final String enterVal = PrologTerm.atomicString(term.getArgument(4));
		final String leaveVal = PrologTerm.atomicString(term.getArgument(5));
		return new VisBHover(svgID, hoverID, attribute, enterVal, leaveVal);
	}

	@Override
	public String toString(){
		return "<Hover changing " + hoverID + "." + hoverAttribute+ " upon enter: "+hoverEnterValue+ " leave: " + hoverLeaveValue + ">";
	}
}
