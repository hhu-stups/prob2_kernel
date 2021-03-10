package de.prob.animator.domainobjects;


import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

import java.util.List;

/**
 * The VisBEvent is designed for the JSON / VisB file
 */
public class VisBHover {
	private String hoverID; // id of the object whose attribute is modified upon hover
	private String hoverOtherID;
	private String hoverAttribute;
	private String hoverEnterValue;
	private String hoverLeaveValue;

	public VisBHover(String hoverID, String hoverOtherID, String hoverAttribute, String hoverEnterValue, String hoverLeaveValue){
		this.hoverID = hoverID;
		this.hoverOtherID = hoverOtherID;
		this.hoverAttribute = hoverAttribute;
		this.hoverEnterValue = hoverEnterValue;
		this.hoverLeaveValue = hoverLeaveValue;
	}

	public String getHoverId() {
		return hoverID;
	}

	public String getHoverOtherID() {
		return hoverOtherID;
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
		final String id = PrologTerm.atomicString(term.getArgument(1));
		final String otherID = PrologTerm.atomicString(term.getArgument(2));
		final String attribute = PrologTerm.atomicString(term.getArgument(3));
		final String enterVal = PrologTerm.atomicString(term.getArgument(4));
		final String leaveVal = PrologTerm.atomicString(term.getArgument(5));
		return new VisBHover(id, otherID, attribute, enterVal, leaveVal);
	}

	@Override
	public String toString(){
		return "<Hover changing " + hoverID + "." + hoverAttribute+ " upon enter: "+hoverEnterValue+ " leave: " + hoverLeaveValue + ">";
	}
}
