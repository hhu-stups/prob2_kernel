package de.prob.check;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.prob.statespace.StateSpace;
import de.tlc4b.TLC4BOption;

public class TLCModelCheckingOptions {

	private static final String TLC_USE_PROB_CONSTANTS = "TLC_USE_PROB_CONSTANTS";
	private static final String TLC_WORKERS = "TLC_WORKERS";
	private static final String MININT = "MININT";
	private static final String MAXINT = "MAXINT";

	private final Map<TLC4BOption, String> options;

	public TLCModelCheckingOptions(Map<TLC4BOption, String> options) {
		this.options = new HashMap<>(options);
	}

	public TLCModelCheckingOptions() {
		this(Collections.emptyMap());
	}

	public static TLCModelCheckingOptions fromPreferences(Map<String, String> preferences) {
		return new TLCModelCheckingOptions()
			.setupConstantsUsingProB("true".equalsIgnoreCase(preferences.get(TLC_USE_PROB_CONSTANTS)))
			.setNumberOfWorkers(preferences.get(TLC_WORKERS))
			.minInt(Integer.parseInt(preferences.get(MININT)))
			.maxInt(Integer.parseInt(preferences.get(MAXINT)));
	}

	public static TLCModelCheckingOptions fromPreferences(StateSpace stateSpace) {
		return fromPreferences(stateSpace.getCurrentPreferences());
	}

	// BEGIN OPTIONS:
	// -workers and -constantssetup are also controlled via preferences
	// use the preference values by default, options can be overwritten here
	// TODO: add remaining TLC4BOptions if required.

	public TLCModelCheckingOptions checkDeadlocks(final boolean value) {
		return changeOption(!value, TLC4BOption.NODEAD);
	}

	public TLCModelCheckingOptions checkGoal(final boolean value) {
		// TODO: check if custom goal is possible
		return changeOption(!value, TLC4BOption.NOGOAL);
	}

	public TLCModelCheckingOptions checkInvariantViolations(final boolean value) {
		return changeOption(!value, TLC4BOption.NOINV);
	}

	public TLCModelCheckingOptions checkAssertions(final boolean value) {
		return changeOption(!value, TLC4BOption.NOASS);
	}

	public TLCModelCheckingOptions checkWelldefinedness(final boolean value) {
		return changeOption(value, TLC4BOption.WDCHECK);
	}

	public TLCModelCheckingOptions proofGuidedModelChecking(final boolean value) {
		return changeOption(value, TLC4BOption.PARINVEVAL);
	}

	public TLCModelCheckingOptions useSymmetry(final boolean value) {
		return changeOption(value, TLC4BOption.SYMMETRY);
	}

	public TLCModelCheckingOptions saveGeneratedFiles(final boolean value) {
		return changeOption(!value, TLC4BOption.TMP);
	}

	public TLCModelCheckingOptions checkLTLAssertions(final boolean value) {
		return changeOption(!value, TLC4BOption.NOLTL);
	}

	public TLCModelCheckingOptions lazyConstants(final boolean value) {
		return changeOption(value, TLC4BOption.LAZYCONSTANTS);
	}

	public TLCModelCheckingOptions noTrace(final boolean value) {
		return changeOption(value, TLC4BOption.NOTRACE);
	}

	public TLCModelCheckingOptions maxInt(final int value) {
		return changeOption(String.valueOf(value), TLC4BOption.MAXINT);
	}

	public TLCModelCheckingOptions defaultSetsize(final int value) {
		return changeOption(String.valueOf(value), TLC4BOption.DEFAULT_SETSIZE);
	}

	public TLCModelCheckingOptions minInt(final int value) {
		return changeOption(String.valueOf(value), TLC4BOption.MININT);
	}

	public TLCModelCheckingOptions setNumberOfWorkers(final String number) {
		return changeOption(number, TLC4BOption.WORKERS);
	}

	public TLCModelCheckingOptions useDepthFirstSearch(final String initialDepth) {
		return changeOption(initialDepth, TLC4BOption.DFID);
	}

	public TLCModelCheckingOptions setupConstantsUsingProB(final boolean value) {
		return changeOption(value, TLC4BOption.CONSTANTSSETUP);
	}

	public TLCModelCheckingOptions checkLTLFormula(final String formula) {
		return changeOption(formula, TLC4BOption.LTLFORMULA);
	}

	public TLCModelCheckingOptions verboseMode(final boolean value) {
		return changeOption(value, TLC4BOption.VERBOSE);
	}

	public TLCModelCheckingOptions silentMode(final boolean value) {
		return changeOption(value, TLC4BOption.SILENT);
	}

	public TLCModelCheckingOptions outputDir(final String outputDir) {
		return changeOption(outputDir, TLC4BOption.OUTPUT);
	}
	// END OPTIONS

	private TLCModelCheckingOptions changeOption(final boolean value, final TLC4BOption o) {
		if (value) {
			this.options.put(o, null);
		} else {
			this.options.remove(o);
		}
		return this;
	}

	private TLCModelCheckingOptions changeOption(final String parameter, final TLC4BOption o) {
		if (parameter != null) {
			this.options.put(o, parameter);
		} else {
			this.options.remove(o);
		}
		return this;
	}

	public Map<TLC4BOption, String> getOptions() {
		if (options.containsKey(TLC4BOption.DFID) && options.containsKey(TLC4BOption.WORKERS)
			&& Integer.parseInt(options.get(TLC4BOption.WORKERS)) > 1) {
			throw new IllegalArgumentException("Depth-First Iterative Deepening mode does not support multiple workers.");
		}
		return options;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		TLCModelCheckingOptions other = (TLCModelCheckingOptions) obj;
		return other.options.equals(this.options);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.options);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("options", this.options)
			.toString();
	}

}
