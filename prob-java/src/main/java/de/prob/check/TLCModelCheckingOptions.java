package de.prob.check;

import com.google.common.base.MoreObjects;
import de.prob.animator.command.GetConstantsPredicateCommand;
import de.prob.statespace.StateSpace;
import de.tlc4b.TLC4BCliOptions;
import de.tlc4b.TLC4BCliOptions.TLCOption;

import java.util.*;

public class TLCModelCheckingOptions {

	private static final String TLC_USE_PROB_CONSTANTS = "TLC_USE_PROB_CONSTANTS";
	private static final String TLC_WORKERS = "TLC_WORKERS";
	private static final String MININT = "MININT";
	private static final String MAXINT = "MAXINT";

	// avoid passing the TLCOption enum outside the API, Class is for type info, e.g. for choice of field type in ProB2-UI
	private final StateSpace stateSpace;
	private final Map<String, Class<?>> allOptions = TLC4BCliOptions.getOptions();
	private final Map<String, String> options = new HashMap<>();

	public TLCModelCheckingOptions(final StateSpace stateSpace) {
		this(stateSpace, new HashMap<>());
	}

	private TLCModelCheckingOptions(final StateSpace stateSpace, final Map<String, String> options) {
		options.forEach(this::addOption);
		this.stateSpace = stateSpace;
		this.setupConstantsUsingProB(stateSpace.getCurrentPreference(TLC_USE_PROB_CONSTANTS).equalsIgnoreCase("true"))
			.setNumberOfWorkers(stateSpace.getCurrentPreference(TLC_WORKERS))
			.minInt(Integer.parseInt(stateSpace.getCurrentPreference(MININT)))
			.maxInt(Integer.parseInt(stateSpace.getCurrentPreference(MAXINT)));
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

	/*public TLCModelCheckingOptions useDepthFirstSearch(final String initialDepth) {
		return changeOption(initialDepth, DFID);
	}*/

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
	// END OPTIONS

	private String getProBConstants() {
		GetConstantsPredicateCommand getConstantsPredicateCommand = new GetConstantsPredicateCommand();
		stateSpace.execute(getConstantsPredicateCommand);
		return getConstantsPredicateCommand.getPredicate();
	}

	private void addOption(final String option, final String value) {
		if (allOptions.containsKey(option)) {
			if (allOptions.get(option) != null && value == null) {
				throw new IllegalArgumentException("Expected argument for option " + option + ".");
			}
			options.put(option, value);
		} else {
			throw new IllegalArgumentException("TLC option '" + option + "' does not exist.");
		}
	}

	private TLCModelCheckingOptions changeOption(final boolean value, final TLCOption o) {
		if (value) {
			this.options.put(o.arg(), null);
		} else {
			this.options.remove(o.arg());
		}
		return this;
	}

	private TLCModelCheckingOptions changeOption(final String parameter, final TLCOption o) {
		if (parameter != null) {
			this.options.put(o.arg(), parameter);
		} else {
			this.options.remove(o.arg());
		}
		return this;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public Map<String, Class<?>> getAllOptions() {
		return allOptions;
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
