package de.prob.check;

import com.google.common.base.MoreObjects;
import de.prob.animator.command.GetConstantsPredicateCommand;
import de.prob.statespace.StateSpace;
import de.tlc4b.TLC4BCliOptions.TLCOption;

import java.util.*;

public class TLCModelCheckingOptions {

	private static final String TLC_USE_PROB_CONSTANTS = "TLC_USE_PROB_CONSTANTS";
	private static final String TLC_WORKERS = "TLC_WORKERS";
	private static final String MININT = "MININT";
	private static final String MAXINT = "MAXINT";

	private final StateSpace stateSpace;
	private final Map<TLCOption, String> options = new HashMap<>();

	/**
	 * default options provided by ProB preferences
	 */
	public TLCModelCheckingOptions(final StateSpace stateSpace) {
		this(stateSpace, new HashMap<>());
		this.setupConstantsUsingProB(stateSpace.getCurrentPreference(TLC_USE_PROB_CONSTANTS).equalsIgnoreCase("true"))
			.setNumberOfWorkers(stateSpace.getCurrentPreference(TLC_WORKERS))
			.minInt(Integer.parseInt(stateSpace.getCurrentPreference(MININT)))
			.maxInt(Integer.parseInt(stateSpace.getCurrentPreference(MAXINT)));
	}

	public TLCModelCheckingOptions(final StateSpace stateSpace, final Map<TLCOption, String> options) {
		this.stateSpace = stateSpace;
		// use only custom options
		this.options.putAll(options);
	}

	// BEGIN OPTIONS:
	// -workers and -constantssetup are also controlled via preferences
	// use the preference values by default, options can be overwritten here
	// TODO: add remaining TLCOptions if required.

	public TLCModelCheckingOptions checkDeadlocks(final boolean value) {
		return changeOption(!value, TLCOption.NODEAD);
	}

	public TLCModelCheckingOptions checkGoal(final boolean value) {
		// TODO: check if custom goal is possible
		return changeOption(!value, TLCOption.NOGOAL);
	}

	public TLCModelCheckingOptions checkInvariantViolations(final boolean value) {
		return changeOption(!value, TLCOption.NOINV);
	}

	public TLCModelCheckingOptions checkAssertions(final boolean value) {
		return changeOption(!value, TLCOption.NOASS);
	}

	public TLCModelCheckingOptions checkWelldefinedness(final boolean value) {
		return changeOption(value, TLCOption.WDCHECK);
	}

	public TLCModelCheckingOptions symmetry(final boolean value) {
		return changeOption(value, TLCOption.SYMMETRY);
	}

	public TLCModelCheckingOptions saveGeneratedFiles(final boolean value) {
		return changeOption(!value, TLCOption.TMP);
	}

	public TLCModelCheckingOptions checkLTLAssertions(final boolean value) {
		return changeOption(!value, TLCOption.NOLTL);
	}

	public TLCModelCheckingOptions lazyConstants(final boolean value) {
		return changeOption(value, TLCOption.LAZYCONSTANTS);
	}

	public TLCModelCheckingOptions noTrace(final boolean value) {
		return changeOption(value, TLCOption.NOTRACE);
	}

	public TLCModelCheckingOptions maxInt(final int value) {
		return changeOption(String.valueOf(value), TLCOption.MAXINT);
	}

	public TLCModelCheckingOptions defaultSetsize(final int value) {
		return changeOption(String.valueOf(value), TLCOption.DEFAULT_SETSIZE);
	}

	public TLCModelCheckingOptions minInt(final int value) {
		return changeOption(String.valueOf(value), TLCOption.MININT);
	}

	public TLCModelCheckingOptions setNumberOfWorkers(final String number) {
		return changeOption(number, TLCOption.WORKERS);
	}

	public TLCModelCheckingOptions useDepthFirstSearch(final String initialDepth) {
		return changeOption(initialDepth, TLCOption.DFID);
	}

	public TLCModelCheckingOptions setupConstantsUsingProB(final boolean value) {
		return changeOption(value ? getProBConstants() : null, TLCOption.CONSTANTSSETUP);
	}

	public TLCModelCheckingOptions checkLTLFormula(final String formula) {
		return changeOption(formula, TLCOption.LTLFORMULA);
	}

	public TLCModelCheckingOptions verboseMode(final boolean value) {
		return changeOption(value, TLCOption.VERBOSE);
	}

	public TLCModelCheckingOptions silentMode(final boolean value) {
		return changeOption(value, TLCOption.SILENT);
	}

	public TLCModelCheckingOptions outputDir(final String outputDir) {
		return changeOption(outputDir, TLCOption.OUTPUT);
	}
	// END OPTIONS

	private String getProBConstants() {
		GetConstantsPredicateCommand getConstantsPredicateCommand = new GetConstantsPredicateCommand();
		stateSpace.execute(getConstantsPredicateCommand);
		return getConstantsPredicateCommand.getPredicate();
	}

	private TLCModelCheckingOptions changeOption(final boolean value, final TLCOption o) {
		if (value) {
			this.options.put(o, null);
		} else {
			this.options.remove(o);
		}
		return this;
	}

	private TLCModelCheckingOptions changeOption(final String parameter, final TLCOption o) {
		if (parameter != null) {
			this.options.put(o, parameter);
		} else {
			this.options.remove(o);
		}
		return this;
	}

	public Map<TLCOption, String> getOptions() {
		if (options.containsKey(TLCOption.DFID) && options.containsKey(TLCOption.WORKERS)
			&& Integer.parseInt(options.get(TLCOption.WORKERS)) > 1) {
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
