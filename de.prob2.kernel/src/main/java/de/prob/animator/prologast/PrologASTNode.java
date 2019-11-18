package de.prob.animator.prologast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.prob.animator.command.GetTopLevelFormulasCommand;
import de.prob.model.representation.AbstractElement;

/**
 * PrologASTNode used to simplify the structure given by prolog
 * 
 * @deprecated Use BVisual2 ({@link GetTopLevelFormulasCommand}) or the Java machine structure ({@link AbstractElement}) instead.
 */
@Deprecated
public abstract class PrologASTNode {
	private final List<PrologASTNode> subnodes;

	PrologASTNode(List<PrologASTNode> subnodes) {
		Objects.requireNonNull(subnodes, "subnodes");
		
		this.subnodes = new ArrayList<>(subnodes);
	}

	public List<PrologASTNode> getSubnodes() {
		return Collections.unmodifiableList(subnodes);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
			.add("subnodes", this.getSubnodes())
			.toString();
	}
}
