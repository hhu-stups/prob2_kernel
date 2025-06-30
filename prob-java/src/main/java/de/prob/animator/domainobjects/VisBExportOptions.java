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

	private final boolean showConstants;
	private final boolean showEvents;
	private final boolean showHeader;
	private final boolean showInvariants;
	private final boolean showSequenceChart;
	private final boolean showSets;
	private final boolean showSource;
	private final boolean showSvgDownloads;
	private final boolean showVariables;
	private final boolean showVersionInfo;
	private final String idPrefix;

	public static final VisBExportOptions DEFAULT = new VisBExportOptions(false, false,
			true, false, false, false, false,
			false, false, true, null);
	// These are the Tcl/Tk default options for history/state exports:
	public static final VisBExportOptions DEFAULT_HISTORY = new VisBExportOptions(true, true,
			true, false, false, true, false,
			false, true, true, null);
	public static final VisBExportOptions DEFAULT_STATES = new VisBExportOptions(false, false,
			true, false, false, false, false,
			false, true, true, null);

	private VisBExportOptions(boolean showConstants, boolean showEvents, boolean showHeader, boolean showInvariants,
	                          boolean showSequenceChart, boolean showSets, boolean showSource, boolean showSvgDownloads,
	                          boolean showVariables, boolean showVersionInfo, String idPrefix) {
		this.showConstants = showConstants;
		this.showEvents = showEvents;
		this.showHeader = showHeader;
		this.showInvariants = showInvariants;
		this.showSequenceChart = showSequenceChart;
		this.showSets = showSets;
		this.showSource = showSource;
		this.showSvgDownloads = showSvgDownloads;
		this.showVariables = showVariables;
		this.showVersionInfo = showVersionInfo;
		this.idPrefix = idPrefix;
	}

	public boolean isShowConstants() {
		return showConstants;
	}

	public VisBExportOptions withShowConstants(boolean showConstants) {
		return new VisBExportOptions(showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}

	public boolean isShowEvents() {
		return showEvents;
	}

	public VisBExportOptions withShowEvents(boolean showEvents) {
		return new VisBExportOptions(this.showConstants, showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowHeader() {
		return showHeader;
	}
	
	public VisBExportOptions withShowHeader(boolean showHeader) {
		return new VisBExportOptions(this.showConstants, this.showEvents, showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}

	public boolean isShowInvariants() {
		return showInvariants;
	}

	public VisBExportOptions withShowInvariants(boolean showInvariants) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}

	public boolean isShowSequenceChart() {
		return showSequenceChart;
	}

	public VisBExportOptions withShowSequenceChart(boolean showSequenceChart) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowSets() {
		return showSets;
	}
	
	public VisBExportOptions withShowSets(boolean showSets) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}

	public boolean isShowSource() {
		return showSource;
	}

	public VisBExportOptions withShowSource(boolean showSource) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}

	public boolean isShowSvgDownloads() {
		return showSvgDownloads;
	}

	public VisBExportOptions withShowSvgDownloads(boolean showSvgDownloads) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, showSvgDownloads, this.showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowVariables() {
		return showVariables;
	}
	
	public VisBExportOptions withShowVariables(boolean showVariables) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, showVariables, this.showVersionInfo, this.idPrefix);
	}
	
	public boolean isShowVersionInfo() {
		return showVersionInfo;
	}
	
	public VisBExportOptions withShowVersionInfo(boolean showVersionInfo) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, showVersionInfo, this.idPrefix);
	}
	
	public String getIdPrefix() {
		return this.idPrefix;
	}
	
	public VisBExportOptions withIdPrefix(String idPrefix) {
		return new VisBExportOptions(this.showConstants, this.showEvents, this.showHeader, this.showInvariants, this.showSequenceChart, this.showSets, this.showSource, this.showSvgDownloads, this.showVariables, this.showVersionInfo, idPrefix);
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
		return this.isShowConstants() == other.isShowConstants()
			&& this.isShowEvents() == other.isShowEvents()
			&& this.isShowHeader() == other.isShowHeader()
			&& this.isShowInvariants() == other.isShowInvariants()
			&& this.isShowSequenceChart() == other.isShowSequenceChart()
			&& this.isShowSets() == other.isShowSets()
			&& this.isShowSource() == other.isShowSource()
			&& this.isShowSvgDownloads() == other.isShowSvgDownloads()
			&& this.isShowVariables() == other.isShowVariables()
			&& this.isShowVersionInfo() == other.isShowVersionInfo()
			&& Objects.equals(this.getIdPrefix(), other.getIdPrefix());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.isShowConstants(),
			this.isShowEvents(),
			this.isShowHeader(),
			this.isShowInvariants(),
			this.isShowSequenceChart(),
			this.isShowSets(),
			this.isShowSource(),
			this.isShowSvgDownloads(),
			this.isShowVariables(),
			this.isShowVersionInfo(),
			this.getIdPrefix()
		);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("showConstants", isShowConstants())
			.add("showEvents", isShowEvents())
			.add("showHeader", this.isShowHeader())
			.add("showInvariants", isShowInvariants())
			.add("showSequenceChart", this.isShowSequenceChart())
			.add("showSets", this.isShowSets())
			.add("showSource", this.isShowSource())
			.add("showSvgDownloads", this.isShowSvgDownloads())
			.add("showVariables", this.isShowVariables())
			.add("showVersionInfo", this.isShowVersionInfo())
			.add("idPrefix", this.getIdPrefix())
			.toString();
	}
	
	public void printProlog(IPrologTermOutput pout) {
		if (this.isShowConstants()) {
			pout.openTerm("show_constants");
			pout.printAtom("all");
			pout.closeTerm();
		}

		if (this.isShowEvents()) {
			pout.openTerm("show_events");
			pout.printAtom("all");
			pout.closeTerm();
		}

		if (!this.isShowHeader()) {
			pout.printAtom("no_header");
		}

		if (this.isShowInvariants()) {
			pout.printAtom("show_invariants");
		}

		if (this.isShowSequenceChart()) {
			pout.printAtom("show_sequence_chart");
		}
		
		if (this.isShowSets()) {
			pout.openTerm("show_sets");
			pout.printAtom("all");
			pout.closeTerm();
		}

		if (this.isShowSource()) {
			pout.printAtom("show_source");
		}

		if (this.isShowSvgDownloads()) {
			pout.printAtom("show_svg_downloads");
		}
		
		if (this.isShowVariables()) {
			pout.openTerm("show_variables");
			pout.printAtom("all");
			pout.closeTerm();
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
