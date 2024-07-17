package de.prob.check;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;

import de.prob.animator.domainobjects.EvalElementType;
import de.prob.animator.domainobjects.IEvalElement;

public class ModelCheckingOptions {
	public enum Options {
		/**
		 * @deprecated This option has unexpected behavior in corner cases.
		 *     Use {@link #searchStrategy(ModelCheckingSearchStrategy)} and {@link ModelCheckingSearchStrategy#BREADTH_FIRST} instead.
		 */
		@Deprecated
		BREADTH_FIRST_SEARCH("breadth_first_search", "breadth first"),
		/**
		 * @deprecated This option has unexpected behavior in corner cases.
		 *     Use {@link #searchStrategy(ModelCheckingSearchStrategy)} and {@link ModelCheckingSearchStrategy#DEPTH_FIRST} instead.
		 */
		@Deprecated
		DEPTH_FIRST_SEARCH("depth_first_search", "depth first"),
		FIND_DEADLOCKS("find_deadlocks", "deadlock check"),
		FIND_INVARIANT_VIOLATIONS("find_invariant_violations", "invariant check"),
		FIND_ASSERTION_VIOLATIONS("find_assertion_violations", "assertion check"),
		/**
		 * @deprecated This enum constant is named incorrectly.
		 *     Use {@link #IGNORE_OTHER_ERRORS} instead,
		 *     which has the same meaning with the correct name.
		 */
		@Deprecated
		FIND_OTHER_ERRORS("ignore_state_errors", "find other errors"),
		IGNORE_OTHER_ERRORS("ignore_state_errors", "ignore other errors"),
		INSPECT_EXISTING_NODES("inspect_existing_nodes", "recheck existing states"),
		STOP_AT_FULL_COVERAGE("stop_at_full_coverage", "stop at full coverage"),
		/**
		 * @deprecated This option is no longer used.
		 *     Partial order reduction is now configured only via ProB preferences.
		 */
		@Deprecated
		PARTIAL_ORDER_REDUCTION("partial_order_reduction", "partial order reduction"),
		/**
		 * @deprecated This option is no longer used.
		 *     Partial guard evaluation is now configured only via ProB preferences.
		 */
		@Deprecated
		PARTIAL_GUARD_EVALUATION("partial_guard_evaluation", "partial guard evaluation"),
		FIND_GOAL("find_goal", "search for goal"),
		;

		private final String prologName;
		private final String description;

		Options(final String prologName, final String description) {
			this.prologName = prologName;
			this.description = description;
		}
		
		public String getPrologName() {
			return this.prologName;
		}
		
		public String getDescription() {
			return description;
		}
	}

	public static final ModelCheckingOptions DEFAULT = new ModelCheckingOptions()
		.checkDeadlocks(true).checkInvariantViolations(true);

	private final ModelCheckingSearchStrategy searchStrategy;
	private final EnumSet<Options> options;
	private final IEvalElement customGoal;
	private final int stateLimit;
	private final Duration timeLimit;

	public ModelCheckingOptions() {
		this.searchStrategy = ModelCheckingSearchStrategy.MIXED_BF_DF;
		options = EnumSet.noneOf(Options.class);
		this.customGoal = null;
		this.stateLimit = -1;
		this.timeLimit = null;
	}

	private ModelCheckingOptions(final ModelCheckingSearchStrategy searchStrategy, final EnumSet<Options> options, final IEvalElement customGoal, final int stateLimit, final Duration timeLimit) {
		this.searchStrategy = searchStrategy;
		this.options = options;
		this.customGoal = customGoal;
		this.stateLimit = stateLimit;
		this.timeLimit = timeLimit;
	}

	public ModelCheckingOptions(final Set<Options> options) {
		this.options = EnumSet.noneOf(Options.class);
		this.options.addAll(options);
		if (this.options.remove(Options.FIND_OTHER_ERRORS)) {
			this.options.add(Options.IGNORE_OTHER_ERRORS);
		}
		this.searchStrategy = searchStrategyFromOptions(this.options);
		this.customGoal = null;
		this.stateLimit = -1;
		this.timeLimit = null;
	}
	
	private static ModelCheckingSearchStrategy searchStrategyFromOptions(final Set<Options> options) {
		if (options.contains(Options.BREADTH_FIRST_SEARCH)) {
			return ModelCheckingSearchStrategy.BREADTH_FIRST;
		} else if (options.contains(Options.DEPTH_FIRST_SEARCH)) {
			return ModelCheckingSearchStrategy.DEPTH_FIRST;
		} else {
			return ModelCheckingSearchStrategy.MIXED_BF_DF;
		}
	}
	
	public ModelCheckingSearchStrategy getSearchStrategy() {
		return this.searchStrategy;
	}

	public ModelCheckingOptions searchStrategy(final ModelCheckingSearchStrategy searchStrategy) {
		Objects.requireNonNull(searchStrategy, "searchStrategy");
		if (this.searchStrategy == searchStrategy) {
			return this;
		}
		final EnumSet<Options> newOptions = EnumSet.copyOf(this.options);
		newOptions.remove(Options.BREADTH_FIRST_SEARCH);
		newOptions.remove(Options.DEPTH_FIRST_SEARCH);
		if (searchStrategy == ModelCheckingSearchStrategy.BREADTH_FIRST) {
			newOptions.add(Options.BREADTH_FIRST_SEARCH);
		} else if (searchStrategy == ModelCheckingSearchStrategy.DEPTH_FIRST) {
			newOptions.add(Options.DEPTH_FIRST_SEARCH);
		}
		return new ModelCheckingOptions(searchStrategy, newOptions, this.getCustomGoal(), this.getStateLimit(), this.getTimeLimit());
	}

	/**
	 * @deprecated This method has unexpected behavior in corner cases.
	 *     Use {@link #searchStrategy(ModelCheckingSearchStrategy)} instead.
	 */
	@Deprecated
	public ModelCheckingOptions breadthFirst(final boolean value) {
		return changeOption(value, Options.BREADTH_FIRST_SEARCH);
	}
	
	/**
	 * @deprecated This method has unexpected behavior in corner cases.
	 *     Use {@link #searchStrategy(ModelCheckingSearchStrategy)} instead.
	 */
	@Deprecated
	public ModelCheckingOptions depthFirst(final boolean value) {
		return changeOption(value, Options.DEPTH_FIRST_SEARCH);
	}

	public ModelCheckingOptions checkDeadlocks(final boolean value) {
		return changeOption(value, Options.FIND_DEADLOCKS);
	}

	public ModelCheckingOptions checkInvariantViolations(final boolean value) {
		return changeOption(value, Options.FIND_INVARIANT_VIOLATIONS);
	}

	public ModelCheckingOptions checkAssertions(final boolean value) {
		return changeOption(value, Options.FIND_ASSERTION_VIOLATIONS);
	}

	public ModelCheckingOptions checkOtherErrors(final boolean value) {
		//Prolog predicate asks to ignore state errors
		return changeOption(!value, Options.IGNORE_OTHER_ERRORS);
	}

	public ModelCheckingOptions recheckExisting(final boolean value) {
		return changeOption(value, Options.INSPECT_EXISTING_NODES);
	}

	public ModelCheckingOptions stopAtFullCoverage(final boolean value) {
		return changeOption(value, Options.STOP_AT_FULL_COVERAGE);
	}

	/**
	 * @deprecated This option is no longer used.
	 *     Partial order reduction is now configured only via ProB preferences.
	 */
	@Deprecated
	public ModelCheckingOptions partialOrderReduction(final boolean value) {
		return changeOption(value, Options.PARTIAL_ORDER_REDUCTION);
	}

	/**
	 * @deprecated This option is no longer used.
	 *     Partial guard evaluation is now configured only via ProB preferences.
	 */
	@Deprecated
	public ModelCheckingOptions partialGuardEvaluation(final boolean value) {
		return changeOption(value, Options.PARTIAL_GUARD_EVALUATION);
	}

	public ModelCheckingOptions checkGoal(final boolean value) {
		return changeOption(value, Options.FIND_GOAL);
	}

	private ModelCheckingOptions changeOption(final boolean value,
			final Options o) {
		if (value == options.contains(o)) {
			return this;
		}

		EnumSet<Options> copyOf = EnumSet.copyOf(options);
		if (value) {
			copyOf.add(o);
		} else {
			copyOf.remove(o);
		}
		final ModelCheckingSearchStrategy newSearchStrategy;
		if (o == Options.BREADTH_FIRST_SEARCH || o == Options.DEPTH_FIRST_SEARCH) {
			newSearchStrategy = searchStrategyFromOptions(copyOf);
		} else {
			newSearchStrategy = this.getSearchStrategy();
		}
		final IEvalElement customGoal;
		if (o == Options.FIND_GOAL && !value) {
			// Remove custom goal when disabling search for goal.
			customGoal = null;
		} else {
			customGoal = this.getCustomGoal();
		}
		return new ModelCheckingOptions(newSearchStrategy, copyOf, customGoal, this.getStateLimit(), this.getTimeLimit());
	}

	public Set<Options> getPrologOptions() {
		return Collections.unmodifiableSet(options);
	}

	public IEvalElement getCustomGoal() {
		return this.customGoal;
	}

	/**
	 * Enable goal search with a custom goal predicate,
	 * overriding any {@code GOAL} defined in the model.
	 * 
	 * @param customGoal the goal to search for,
	 *     or {@code null} to use the {@code GOAL} definition from the model
	 * @return modified copy of {@code this}
	 */
	public ModelCheckingOptions customGoal(final IEvalElement customGoal) {
		if (Objects.equals(this.getCustomGoal(), customGoal)) {
			return this;
		}
		if (customGoal != null && customGoal.getKind() != EvalElementType.PREDICATE) {
			throw new IllegalArgumentException("Model checking goal must be a predicate, not " + customGoal.getKind());
		}
		return new ModelCheckingOptions(this.getSearchStrategy(), options, customGoal, this.getStateLimit(), this.getTimeLimit())
			.checkGoal(true);
	}

	public int getStateLimit() {
		return this.stateLimit;
	}

	public ModelCheckingOptions stateLimit(final int stateLimit) {
		return new ModelCheckingOptions(this.getSearchStrategy(), options, this.getCustomGoal(), stateLimit < 0 ? -1 : stateLimit, this.getTimeLimit());
	}

	public Duration getTimeLimit() {
		return this.timeLimit;
	}

	public ModelCheckingOptions timeLimit(final Duration timeLimit) {
		return new ModelCheckingOptions(this.getSearchStrategy(), options, this.getCustomGoal(), this.getStateLimit(), timeLimit);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		ModelCheckingOptions other = (ModelCheckingOptions) obj;
		return this.getSearchStrategy() == other.getSearchStrategy()
			&& other.options.equals(this.options)
			&& Objects.equals(this.getCustomGoal(), other.getCustomGoal())
			&& this.getStateLimit() == other.getStateLimit()
			&& Objects.equals(this.getTimeLimit(), other.getTimeLimit());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
			this.getSearchStrategy(),
			this.options,
			this.getCustomGoal(),
			this.getStateLimit(),
			this.getTimeLimit()
		);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("searchStrategy", this.getSearchStrategy())
			.add("options", this.options)
			.add("customGoal", this.getCustomGoal())
			.add("stateLimit", this.getStateLimit())
			.add("timeLimit", this.getTimeLimit())
			.toString();
	}

}
