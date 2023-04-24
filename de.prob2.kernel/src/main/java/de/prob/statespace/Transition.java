package de.prob.statespace;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;
import de.prob.animator.domainobjects.EvalOptions;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.FormulaTranslationMode;
import de.prob.formula.PredicateBuilder;
import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * <p>
 * Stores the information for a given Operation. This includes operation id
 * (id), operation name (name), the source state (src), and the destination
 * state (dest), as well as a list of parameters.
 * </p>
 * 
 * <p>
 * Note: This class retains a reference to the {@link StateSpace} object to
 * which it belongs. In order to ensure that the garbage collector works
 * correctly when cleaning up a {@link StateSpace} object make sure that all
 * {@link Transition} objects are correctly dereferenced.
 * </p>
 * 
 * @author joy
 */
public class Transition {
	public static final String PARTIAL_SETUP_CONSTANTS_NAME = "$partial_setup_constants";
	public static final String SETUP_CONSTANTS_NAME = "$setup_constants";
	public static final String INITIALISE_MACHINE_NAME = "$initialise_machine";
	
	private static final BiMap<String, String> PRETTY_NAME_MAP;
	
	static {
		final BiMap<String, String> prettyNameMap = HashBiMap.create();
		prettyNameMap.put(PARTIAL_SETUP_CONSTANTS_NAME, "PARTIAL_SETUP_CONSTANTS");
		prettyNameMap.put(SETUP_CONSTANTS_NAME, "SETUP_CONSTANTS");
		prettyNameMap.put(INITIALISE_MACHINE_NAME, "INITIALISATION");
		PRETTY_NAME_MAP = Maps.unmodifiableBiMap(prettyNameMap);
	}
	
	private static final Set<String> ARTIFICIAL_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		PARTIAL_SETUP_CONSTANTS_NAME, SETUP_CONSTANTS_NAME, INITIALISE_MACHINE_NAME
	)));
	
	public static final EvalOptions OLD_DEFAULT_EVAL_OPTIONS = EvalOptions.DEFAULT
		.withExpand(FormulaExpand.TRUNCATE)
		// Old code returned ASCII instead of Unicode - keep it that way for compatibility.
		.withMode(FormulaTranslationMode.ASCII);

	private final StateSpace stateSpace;
	private final String id;
	private final String name;
	private final State src;
	private final State dest;
	private final Map<EvalOptions, EvaluatedTransitionInfo> evaluatedInfos;
	private List<BValue> translatedParams;
	private List<BValue> translatedRetV;
	private String predicateString;

	private Transition(final StateSpace stateSpace, final String id, final String name, final State src,
			final State dest) {
		this.stateSpace = stateSpace;
		this.id = id;
		this.name = name;
		this.src = src;
		this.dest = dest;
		this.evaluatedInfos = new HashMap<>();
	}

	public static String prettifyName(final String name) {
		return PRETTY_NAME_MAP.getOrDefault(name, name);
	}

	public static String unprettifyName(final String name) {
		return PRETTY_NAME_MAP.inverse().getOrDefault(name, name);
	}

	public static boolean isArtificialTransitionName(final String name) {
		return ARTIFICIAL_NAMES.contains(name);
	}

	/**
	 * @return String identifier associated with this Operation
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return name of this operation
	 */
	public String getName() {
		return name;
	}

	public String getPrettyName() {
		return prettifyName(this.getName());
	}

	/**
	 * @return the {@link State} reference to the source node of this transition
	 */
	public State getSource() {
		return src;
	}

	/**
	 * @return the {@link State} reference to the destination node of this
	 *         operation
	 */
	public State getDestination() {
		return dest;
	}

	/**
	 * The list of parameters is not filled by default. If the parameter list
	 * has not yet been filled, ProB will be contacted to lazily fill the
	 * parameter list via the {@link #evaluate()} method.
	 * 
	 * @return list of values for the parameters represented as strings
	 * @deprecated
	 */
	@Deprecated
	public List<String> getParams() {
		return getParameterValues();
	}

	/**
	 * <p>
	 * The list of parameters is not filled by default. If the parameter list
	 * has not yet been filled, ProB will be contacted to lazily fill the
	 * parameter list via the {@link #evaluate()} method.
	 * </p>
	 * <p>
	 * This method always fully expands and evaluates all parameter values.
	 * Although this is safe, it may be very slow for large or complex values.
	 * Consider using {@link #evaluate(EvalOptions)} instead for more control over the evaluation options.
	 * </p>
	 *
	 * @return list of values for the parameters represented as strings
	 */
	public List<String> getParameterValues() {
		return evaluate(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.EXPAND)).getParameterValues();
	}

	public List<BValue> getTranslatedParams() throws TranslationException {
		if (translatedParams == null) {
			List<BValue> list = new ArrayList<>();
			for (String s : getParameterValues()) {
				list.add(Translator.translate(s));
			}
			translatedParams = list;
		}
		return translatedParams;
	}

	/**
	 * <p>
	 * The list of return values is not filled by default. If the return value
	 * list has not yet been filled, ProB will be contacted to lazily fill the
	 * list via the {@link #evaluate()} method.
	 * </p>
	 * <p>
	 * This method always fully expands and evaluates all return values.
	 * Although this is safe, it may be very slow for large or complex values.
	 * Consider using {@link #evaluate(EvalOptions)} instead for more control over the evaluation options.
	 * </p>
	 * 
	 * @return list of return values of the operation represented as strings.
	 */
	public List<String> getReturnValues() {
		return evaluate(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.EXPAND)).getReturnValues();
	}

	public StateSpace getStateSpace() {
		return this.stateSpace;
	}

	public List<BValue> getTranslatedReturnValues() throws TranslationException {
		if (translatedRetV == null) {
			List<BValue> list = new ArrayList<>();
			for (String s : getReturnValues()) {
				list.add(Translator.translate(s));
			}
			translatedRetV = list;
		}
		return translatedRetV;
	}

	@Override
	public String toString() {
		return name;
	}

	// TODO Replace this with evaluate(OLD_DEFAULT_EVAL_OPTIONS) once evaluate supports fuzzy matching
	private EvaluatedTransitionInfo getCachedEvalInfoForRep() {
		if (this.isEvaluated(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.EXPAND))) {
			return this.evaluate(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.EXPAND));
		} else if (this.isEvaluated(OLD_DEFAULT_EVAL_OPTIONS)) {
			return this.evaluate(OLD_DEFAULT_EVAL_OPTIONS);
		} else {
			return null;
		}
	}

	/**
	 * Return a string representation of the transition,
	 * including all parameter and return values if known.
	 * This method does <i>not</i> evaluate parameter and return values automatically.
	 * If this transition hasn't been evaluated yet,
	 * only the operation name is returned.
	 * Consider using {@link #evaluate(EvalOptions)} instead to ensure that the transition has been evaluated with the desired options.
	 * 
	 * @return the String representation of the operation.
	 */
	public String getRep() {
		final EvaluatedTransitionInfo info = this.getCachedEvalInfoForRep();
		if (info == null) {
			return this.getName();
		} else {
			return info.getRep();
		}
	}

	/**
	 * Uses {@link #getParameterPredicates()} to calculate a string predicate
	 * representing the value of the parameters for this transition.
	 * 
	 * @return the {@link String} predicate that represents the value of the
	 *         parameters for this transition.
	 */
	public String getParameterPredicate() {
		if (predicateString != null) {
			return predicateString;
		}
		predicateString = new PredicateBuilder().addList(getParameterPredicates()).toString();
		return predicateString;
	}

	/**
	 * @return a list of string predicates representing the value of the
	 *         parameters for this transition
	 */
	public List<String> getParameterPredicates() {
		if (isArtificialTransition()) {
			return Collections.emptyList();
		}
		evaluate(FormulaExpand.EXPAND);
		List<String> predicates = new ArrayList<>();
		List<String> paramNames = getParameterNames();
		List<String> paramValues = getParameterValues();
		if (paramNames.size() == paramValues.size()) {
			for (int i = 0; i < paramNames.size(); i++) {
				predicates.add(paramNames.get(i) + " = " + paramValues.get(i));
			}
		}
		return predicates;
	}

	public List<String> getParameterNames() {
		OperationInfo operationInfo = stateSpace.getLoadedMachine().getMachineOperationInfo(getName());
		return operationInfo == null ? new ArrayList<>() : operationInfo.getParameterNames();
	}

	/**
	 * Like {@link #getRep()}, but uses {@link #getPrettyName()} for the transition name.
	 * This method does <i>not</i> evaluate parameter and return values automatically.
	 * If this transition hasn't been evaluated yet,
	 * only the operation name is returned.
	 * Consider using {@link #evaluate(EvalOptions)} instead to ensure that the transition has been evaluated with the desired options.
	 * 
	 * @return the pretty string representation of the operation
	 */
	public String getPrettyRep() {
		final EvaluatedTransitionInfo info = this.getCachedEvalInfoForRep();
		if (info == null) {
			return this.getPrettyName();
		} else {
			return info.getPrettyRep();
		}
	}

	public boolean isArtificialTransition() {
		return isArtificialTransitionName(this.getName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final Transition other = (Transition) obj;
		return Objects.equals(this.getId(), other.getId())
				&& Objects.equals(this.getSource(), other.getSource())
				&& Objects.equals(this.getDestination(), other.getDestination());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getId(), this.getSource(), this.getDestination());
	}

	/**
	 * @param that
	 *            {@link Transition} with which this {@link Transition} should
	 *            be compared
	 * @return if the name and parameters of the {@link Transition}s are
	 *         equivalent
	 */
	public boolean isSame(final Transition that) {
		evaluate(FormulaExpand.EXPAND);
		that.evaluate(FormulaExpand.EXPAND);
		return that.getName().equals(this.getName()) && that.getParameterValues().equals(this.getParameterValues());
	}

	/**
	 * For use only by {@link GetOpFromId} to cache the {@link EvaluatedTransitionInfo} in the transition object.
	 * 
	 * @param options evaluation options that were passed to {@link GetOpFromId}
	 * @param info result of the {@link GetOpFromId} command
	 */
	void addEvaluatedInfo(final EvalOptions options, final EvaluatedTransitionInfo info) {
		this.evaluatedInfos.put(options, info);
	}

	/**
	 * The {@link Transition} is checked to see if the name, parameters, and
	 * return values have been retrieved from ProB yet. If not, the retrieval
	 * takes place via the {@link GetOpFromId} command and the missing values
	 * are set.
	 * 
	 * @return {@code this}
	 */
	public Transition evaluate() {
		return evaluate(FormulaExpand.TRUNCATE);
	}

	/**
	 * @deprecated {@link #evaluate(EvalOptions)} now supports more options than just "truncated" and "expanded".
	 *     Use {@link #isEvaluated(EvalOptions)} instead to check whether the transition has been evaluated with the desired options.
	 */
	@Deprecated
	public boolean canBeEvaluated(final FormulaExpand expansion) {
		return !this.isEvaluated(OLD_DEFAULT_EVAL_OPTIONS.withExpand(expansion));
	}

	public Transition evaluate(final FormulaExpand expansion) {
		this.evaluate(OLD_DEFAULT_EVAL_OPTIONS.withExpand(expansion));
		return this;
	}

	/**
	 * Retrieve the transition's parameter and return values from ProB.
	 * The results are cached,
	 * so if the transition has already ben evaluated before with the same options,
	 * that result is returned directly without calling ProB again.
	 * 
	 * @param options options to use when pretty-printing the parameter and return values
	 * @return the transition's parameter and return values
	 */
	public EvaluatedTransitionInfo evaluate(final EvalOptions options) {
		// TODO Reuse cached values where the options don't match exactly, but are strictly better, e. g.:
		// If FormulaExpand.TRUNCATE was requested
		// and the transition was already evaluated with FormulaExpand.EXPAND,
		// then that result should be returned instead of re-evaluating with TRUNCATE.
		// If FormulaExpand.EXPAND was requested
		// and the transition was already evaluated with FormulaExpand.TRUNCATE,
		// then the TRUNCATE result should be replaced by the EXPAND result instead of caching both.
		if (!this.isEvaluated(options)) {
			stateSpace.execute(new GetOpFromId(this, options));
		}
		return this.evaluatedInfos.get(options);
	}

	/**
	 * Check whether this transition has already been evaluated with the given options.
	 * If {@code true} is returned,
	 * calling {@link #evaluate(EvalOptions)} with the same options
	 * will return the cached results instead of re-evaluating the transition.
	 * 
	 * @param options desired evaluation options for parameter and return values
	 * @return whether evaluation results have already been cached for the given options
	 */
	public boolean isEvaluated(final EvalOptions options) {
		return this.evaluatedInfos.containsKey(options);
	}

	/**
	 * <p>
	 * Check whether this transition has been evaluated.
	 * </p>
	 * <p>
	 * For compatibility, this method only recognizes evaluation using the old default options used by {@link #evaluate()} and {@link #evaluate(FormulaExpand)}.
	 * This may change in the future.
	 * Consider using {@link #isEvaluated(EvalOptions)} instead to check whether the transition has been evaluated with the desired options.
	 * </p>
	 * 
	 * @return whether or not the name, parameters, and return values have yet
	 *         been retrieved from ProB
	 */
	public boolean isEvaluated() {
		return isEvaluated(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.TRUNCATE))
			|| isEvaluated(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.EXPAND));
	}

	/**
	 * @deprecated {@link #evaluate(EvalOptions)} now supports more options than just "truncated" and "expanded".
	 *     Use {@link #isEvaluated(EvalOptions)} instead to check whether the transition has been evaluated with the desired options.
	 */
	@Deprecated
	public boolean isTruncated() {
		return isEvaluated(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.TRUNCATE))
			&& !isEvaluated(OLD_DEFAULT_EVAL_OPTIONS.withExpand(FormulaExpand.EXPAND));
	}

	/**
	 * Calls {@link State#getStateRep} to calculate the SHA-1 of the destination
	 * state.
	 * 
	 * @return A SHA-1 hash of the target state in String format.
	 * @throws NoSuchAlgorithmException
	 *             if no SHA-1 provider is found
	 * @deprecated This method has a few pitfalls and should be avoided.
	 *     The hash only takes the transition's destination state into account,
	 *     not any other aspects of the transition itself
	 *     (like the operation name and parameter values),
	 *     and is potentially dependent on the system default charset
	 *     (e. g. if the model contains non-ASCII identifiers).
	 *     The hash value is also returned in a non-standard format (base 36).
	 *     This method is currently kept to support Groovy trace files (see {@link TraceConverter}),
	 *     but shouldn't be used in any new code.
	 */
	@Deprecated
	public String sha() throws NoSuchAlgorithmException {
		evaluate(FormulaExpand.EXPAND);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(getDestination().getStateRep().getBytes());
		return new BigInteger(1, md.digest()).toString(Character.MAX_RADIX);
	}

	/**
	 * Creates an artificial transition that is to be added to the
	 * {@link StateSpace}.
	 * 
	 * @param s
	 *            {@link StateSpace} object to which this operation belongs
	 * @param transId
	 *            String operation id
	 * @param description
	 *            String description of the operation
	 * @param srcId
	 *            String id of source node
	 * @param destId
	 *            String id of destination node
	 * @return {@link Transition} representation of given information
	 */
	public static Transition generateArtificialTransition(final StateSpace s, final String transId,
			final String description, final String srcId, final String destId) {
		return new Transition(s, transId, description, s.addState(srcId), s.addState(destId));
	}

	/**
	 * @param s
	 *            StateSpace object to which this operation belongs
	 * @param cpt
	 *            {@link CompoundPrologTerm} representation of the operation
	 *            which contains the transition id, source id, and destination
	 *            id
	 * @return {@link Transition} object representing the information about the
	 *         operation
	 */
	public static Transition createTransitionFromCompoundPrologTerm(final StateSpace s, final CompoundPrologTerm cpt) {
		String opId = Transition.getIdFromPrologTerm(cpt.getArgument(1));
		String name = BindingGenerator.getCompoundTerm(cpt.getArgument(2), 0).getFunctor().intern();
		String srcId = Transition.getIdFromPrologTerm(cpt.getArgument(3));
		String destId = Transition.getIdFromPrologTerm(cpt.getArgument(4));
		return new Transition(s, opId, name, s.addState(srcId), s.addState(destId));
	}

	/**
	 * Takes a {@link PrologTerm} representation of a transition id and
	 * translates it to a string value.
	 * 
	 * @param destTerm
	 *            {@link PrologTerm} representing the Transition Id
	 * @return String representation of the Transition Id
	 */
	public static String getIdFromPrologTerm(final PrologTerm destTerm) {
		if (destTerm instanceof IntegerPrologTerm) {
			return BindingGenerator.getInteger(destTerm).getValue().toString();
		}
		return destTerm.getFunctor();
	}
}
