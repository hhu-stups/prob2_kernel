package de.prob.animator.domainobjects;

import com.google.common.base.MoreObjects;
import de.prob.prolog.output.IPrologTermOutput;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A set of meta information for a {@link de.prob.animator.command.VisBPerformClickCommand}.</p>
 * <p>{@link VisBClickMetaInfos} objects are immutable.</p>
 */
public final class VisBClickMetaInfos {
	private final boolean metaKey;
	private final int pageX;
	private final int pageY;
	private final boolean shiftKey;

	private final Map<String,String> javaScriptVariables;

	public VisBClickMetaInfos(boolean metaKey, int pageX, int pageY, boolean shiftKey,
	                           Map<String,String> javaScriptVariables) {
		this.metaKey = metaKey;
		this.pageX = pageX;
		this.pageY = pageY;
		this.shiftKey = shiftKey;
		this.javaScriptVariables = new HashMap<>(javaScriptVariables);
	}

	public boolean isMetaKey() {
		return metaKey;
	}

	public int getPageX() {
		return pageX;
	}

	public int getPageY() {
		return pageY;
	}

	public boolean isShiftKey() {
		return shiftKey;
	}

	public Map<String, String> getJavaScriptVariables() {
		return Collections.unmodifiableMap(javaScriptVariables);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		VisBClickMetaInfos other = (VisBClickMetaInfos)obj;
		return this.isMetaKey() == other.isMetaKey()
			&& this.getPageX() == other.getPageX()
			&& this.getPageY() == other.getPageY()
			&& this.isShiftKey() == other.isShiftKey()
			&& this.getJavaScriptVariables() == other.getJavaScriptVariables();
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			this.isMetaKey(),
			this.getPageX(),
			this.getPageY(),
			this.isShiftKey(),
			this.getJavaScriptVariables()
		);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("metaKey", this.isMetaKey())
			.add("pageX", this.getPageX())
			.add("pageY", this.getPageY())
			.add("shiftKey", this.isShiftKey())
			.add("javaScriptVariables", this.getJavaScriptVariables())
			.toString();
	}

	public void printProlog(IPrologTermOutput pout) {
		if (this.isMetaKey()) {
			pout.printAtom("meta_key");
		}

		pout.openTerm("pageX");
		pout.printNumber(this.getPageX());
		pout.closeTerm();

		pout.openTerm("pageY");
		pout.printNumber(this.getPageY());
		pout.closeTerm();
		
		if (this.isShiftKey()) {
			pout.printAtom("shift_key");
		}

		this.getJavaScriptVariables().forEach((var,value) -> {
			pout.openTerm("js_var");
			pout.printAtom(var);
			pout.printAtom(value);
			pout.closeTerm();
		});
	}
}
