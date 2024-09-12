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
	public static final String AUTO_ID_PREFIX = "auto";
	
	private final boolean showHeader;
	private final boolean showSets;
	private final boolean showSource;
	private final boolean showConstants;
	private final boolean showVariables;
	private final boolean showVersionInfo;
	private final String idPrefix;
	
	public static final VisBExportOptions DEFAULT = new VisBExportOptions(true, false, false, false, false, true, null);
	
	private VisBExportOptions(boolean showHeader, boolean showSets, boolean showSource, boolean showConstants, boolean showVariables, boolean showVersionInfo, String idPrefix) {
		this.showHeader = showHeader;
		this.showSets = showSets;
		this.showSource = showSource;
		this.showConstants = showConstants;
		this.showVariables = showVariables;
		this.showVersionInfo = showVersionInfo;
		this.idPrefix = idPrefix;
	}
	
	public boolean isShowHeader() {
		return showHeader;
	}
	
	public VisBExportOptions withShowHeader(boolean showHeader) {
		return new VisBExportOptions(showHeader, this.showSets, this.showSource, this.showConstants, this.showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowSets() {
		return showSets;
	}
	
	public VisBExportOptions withShowSets(boolean showSets) {
		return new VisBExportOptions(this.showHeader, showSets, this.showSource, this.showConstants, this.showVariables, this.showVersionInfo, this.idPrefix);
	}

	public boolean isShowSource() {
		return showSource;
	}

	public VisBExportOptions withShowSource(boolean showSource) {
		return new VisBExportOptions(this.showHeader, this.showSets, showSource, this.showConstants, this.showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowConstants() {
		return showConstants;
	}
	
	public VisBExportOptions withShowConstants(boolean showConstants) {
		return new VisBExportOptions(this.showHeader, this.showSets, this.showSource, showConstants, this.showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowVariables() {
		return showVariables;
	}
	
	public VisBExportOptions withShowVariables(boolean showVariables) {
		return new VisBExportOptions(this.showHeader, this.showSets, this.showSource, this.showConstants, showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowVersionInfo() {
		return showVersionInfo;
	}
	
	public VisBExportOptions withShowVersionInfo(boolean showVersionInfo) {
		return new VisBExportOptions(this.showHeader, this.showSets, this.showSource, this.showConstants, this.showVariables, showVersionInfo, this.idPrefix);
	}
	
	public String getIdPrefix() {
		return this.idPrefix;
	}
	
	public VisBExportOptions withIdPrefix(String idPrefix) {
		return new VisBExportOptions(this.showHeader, this.showSets, this.showSource, this.showConstants, this.showVariables, this.showVersionInfo, idPrefix);
	}
	
	public VisBExportOptions withAutoIdPrefix() {
		return this.withIdPrefix(AUTO_ID_PREFIX);
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
		return this.isShowHeader() == other.isShowHeader()
			&& this.isShowSets() == other.isShowSets()
			&& this.isShowSource() == other.isShowSource()
			&& this.isShowConstants() == other.isShowConstants()
			&& this.isShowVariables() == other.isShowVariables()
			&& this.isShowVersionInfo() == other.isShowVersionInfo()
			&& Objects.equals(this.getIdPrefix(), other.getIdPrefix());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.isShowHeader(),
			this.isShowSets(),
			this.isShowSource(),
			this.isShowConstants(),
			this.isShowVariables(),
			this.isShowVersionInfo(),
			this.getIdPrefix()
		);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("showHeader", this.isShowHeader())
			.add("showSets", this.isShowSets())
			.add("showSource", this.isShowSource())
			.add("showConstants", this.isShowConstants())
			.add("showVariables", this.isShowVariables())
			.add("showVersionInfo", this.isShowVersionInfo())
			.add("idPrefix", this.getIdPrefix())
			.toString();
	}
	
	public void printProlog(IPrologTermOutput pout) {
		if (!this.isShowHeader()) {
			pout.printAtom("no_header");
		}
		
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

		if (this.isShowSource()) {
			pout.printAtom("show_source");
		}
		
		if (!this.isShowVersionInfo()) {
			pout.printAtom("no_version_info");
		}
		
		if (this.getIdPrefix() != null) {
			pout.openTerm("id_namespace_prefix");
			pout.printAtom(this.getIdPrefix());
			pout.closeTerm();
		}
	}
}
