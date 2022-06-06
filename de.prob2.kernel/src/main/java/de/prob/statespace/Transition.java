package de.prob.statespace;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

	private final StateSpace stateSpace;
	private final String id;
	private final String name;
	private final State src;
	private final State dest;
	private List<String> params;
	private List<String> returnValues;
	private List<BValue> translatedParams;
	private List<BValue> translatedRetV;
	private String rep;
	private String prettyRep;
	private boolean evaluated;
	private FormulaExpand formulaExpansion;
	private final FormalismType formalismType;
	private String predicateString;

	private Transition(final StateSpace stateSpace, final String id, final String name, final State src,
			final State dest) {
		this.stateSpace = stateSpace;
		this.id = id;
		this.name = name;
		this.src = src;
		this.dest = dest;
		this.evaluated = false;
		this.rep = name;
		formalismType = stateSpace.getModel().getFormalismType();
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

	public List<String> getParameterValues() {
		evaluate(FormulaExpand.EXPAND);
		return params;
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
	 * The list of return values is not filled by default. If the return value
	 * list has not yet been filled, ProB will be contacted to lazily fill the
	 * list via the {@link #evaluate()} method.
	 * 
	 * @return list of return values of the operation represented as strings.
	 */
	public List<String> getReturnValues() {
		evaluate(FormulaExpand.EXPAND);
		return returnValues;
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

	/**
	 * @return the String representation of the operation.
	 */
	public String getRep() {
		return rep;
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
		if (paramNames.size() == this.params.size()) {
			for (int i = 0; i < paramNames.size(); i++) {
				predicates.add(paramNames.get(i) + " = " + this.params.get(i));
			}
		}
		return predicates;
	}

	public List<String> getParameterNames() {
		OperationInfo operationInfo = stateSpace.getLoadedMachine().getMachineOperationInfo(getName());
		return operationInfo == null ? new ArrayList<>() : operationInfo.getParameterNames();
	}

	private String createRep(final String name, final List<String> params, final List<String> returnVals) {
		if (formalismType.equals(FormalismType.CSP)) {
			if (params.isEmpty()) {
				return name;
			} else {
				return name + '.' + String.join(".", params);
			}
		} else {
			String retVals = returnVals.isEmpty() ? "" : String.join(",", returnVals) + " <-- ";
			return retVals + name + '(' + String.join(",", params) + ')';
		}
	}

	public String getPrettyRep() {
		return this.prettyRep;
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
		return that.getName().equals(name) && that.getParameterValues().equals(params);
	}

	/**
	 * The {@link Transition} is checked to see if the name, parameters, and
	 * return values have been retrieved from ProB yet. If not, the retrieval
	 * takes place via the {@link GetOpFromId} command and the missing values
	 * are set.
	 * 
	 * @return {@code this}
	 * @deprecated Use {@link #evaluate(FormulaExpand)} with an explicit {@link FormulaExpand} argument instead
	 */
	@Deprecated
	public Transition evaluate() {
		return evaluate(FormulaExpand.TRUNCATE);
	}

	public boolean canBeEvaluated(final FormulaExpand expansion) {
		return !evaluated || this.formulaExpansion == FormulaExpand.TRUNCATE && expansion == FormulaExpand.EXPAND;
	}

	public Transition evaluate(final FormulaExpand expansion) {
		if (canBeEvaluated(expansion)) {
			GetOpFromId command = new GetOpFromId(this, expansion);
			stateSpace.execute(command);
		}
		return this;
	}

	public EvaluatedTransitionInfo evaluate(final EvalOptions options) {
		// TODO Support caching results like in the other overload
		final GetOpFromId cmd = new GetOpFromId(this, options);
		stateSpace.execute(cmd);
		return cmd.getInfo();
	}

	/**
	 * Check whether this transition has been evaluated.
	 * 
	 * @return whether or not the name, parameters, and return values have yet
	 *         been retrieved from ProB
	 */
	public boolean isEvaluated() {
		return evaluated;
	}

	public boolean isTruncated() {
		return formulaExpansion == FormulaExpand.TRUNCATE;
	}

	/**
	 * Calls {@link State#getStateRep} to calculate the SHA-1 of the destination
	 * state.
	 * 
	 * @return A SHA-1 hash of the target state in String format.
	 * @throws NoSuchAlgorithmException
	 *             if no SHA-1 provider is found
	 */
	public String sha() throws NoSuchAlgorithmException {
		evaluate(FormulaExpand.EXPAND);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(getDestination().getStateRep().getBytes());
		return new BigInteger(1, md.digest()).toString(Character.MAX_RADIX);
	}

	/**
	 * Sets the values for the fields in this class. This should ONLY be called
	 * by the {@link GetOpFromId} command during retrieval of the values from
	 * Prolog. For this reason, it is package private
	 * 
	 * @param params
	 *            - {@link List} of {@link String} parameters
	 * @param returnValues
	 *            - {@link List} of {@link String} return values
	 */
	void setInfo(final FormulaExpand expansion, final List<String> params, final List<String> returnValues) {
		this.formulaExpansion = expansion;
		this.params = params;
		this.returnValues = returnValues;
		this.rep = createRep(name, params, returnValues);
		this.prettyRep = createRep(this.getPrettyName(), params, returnValues);
		evaluated = true;
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
