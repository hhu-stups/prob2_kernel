package de.prob.animator.domainobjects;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * <p>A set of options controlling what is included in a VisB HTML export.</p>
 * <p>
 * {@link VisBExportOptions} objects are immutable.
 * There is no public constructor - to create a new set of options,
 * start with the {@link #DEFAULT} options
 * and change the desired settings using the {@code with...} methods.
 * </p>
 */
public final class VisBExportOptions {
	private final boolean showSets;
	private final boolean showConstants;
	private final boolean showVariables;
	private final boolean showVersionInfo;
	
	public static final VisBExportOptions DEFAULT = new VisBExportOptions(false, false, false, true);
	
	private VisBExportOptions(boolean showSets, boolean showConstants, boolean showVariables, boolean showVersionInfo) {
		this.showSets = showSets;
		this.showConstants = showConstants;
		this.showVariables = showVariables;
		this.showVersionInfo = showVersionInfo;
	}
	
	public boolean isShowSets() {
		return showSets;
	}
	
	public VisBExportOptions withShowSets(boolean showSets) {
		return new VisBExportOptions(showSets, this.showConstants, this.showVariables, this.showVersionInfo);
	}
	
	public boolean isShowConstants() {
		return showConstants;
	}
	
	public VisBExportOptions withShowConstants(boolean showConstants) {
		return new VisBExportOptions(this.showSets, showConstants, this.showVariables, this.showVersionInfo);
	}
	
	public boolean isShowVariables() {
		return showVariables;
	}
	
	public VisBExportOptions withShowVariables(boolean showVariables) {
		return new VisBExportOptions(this.showSets, this.showConstants, showVariables, this.showVersionInfo);
	}
	
	public boolean isShowVersionInfo() {
		return showVersionInfo;
	}
	
	public VisBExportOptions withShowVersionInfo(boolean showVersionInfo) {
		return new VisBExportOptions(this.showSets, this.showConstants, this.showVariables, showVersionInfo);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		VisBExportOptions other = (VisBExportOptions)obj;
		return this.isShowSets() == other.isShowSets()
			&& this.isShowConstants() == other.isShowConstants()
			&& this.isShowVariables() == other.isShowVariables()
			&& this.isShowVersionInfo() == other.isShowVersionInfo();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.isShowSets(),
			this.isShowConstants(),
			this.isShowVariables(),
			this.isShowVersionInfo()
		);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("showSets", this.isShowSets())
			.add("showConstants", this.isShowConstants())
			.add("showVariables", this.isShowVariables())
			.add("showVersionInfo", this.isShowVersionInfo())
			.toString();
	}
	
	public void printProlog(IPrologTermOutput pout) {
		if (this.isShowSets()) {
			pout.openTerm("show_sets");
			pout.printAtom("all");
			pout.closeTerm();
		}
		
		if (this.isShowConstants()) {
			pout.openTerm("show_constants");
			pout.printAtom("all");
			pout.closeTerm();
		}
		
		if (this.isShowVariables()) {
			pout.openTerm("show_variables");
			pout.printAtom("all");
			pout.closeTerm();
		}
		
		if (!this.isShowVersionInfo()) {
			pout.printAtom("no_version_info");
		}
	}
}
