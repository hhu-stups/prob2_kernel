package de.prob.animator.domainobjects;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.hhu.stups.prob.translator.BValue;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;
import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import groovy.lang.MissingPropertyException;

public class EvalResult extends AbstractEvalResult {
	public static final  EvalResult TRUE = new EvalResult("TRUE", Collections.emptyMap());
	public static final  EvalResult FALSE = new EvalResult("FALSE", Collections.emptyMap());
	private static final  HashMap<String, EvalResult> formulaCache = new HashMap<>();

	private final String value;
	private final Map<String, String> solutions;

	public EvalResult(final String value, final Map<String, String> solutions) {
		super();
		this.value = value;
		this.solutions = solutions;
	}

	public Map<String, String> getSolutions() {
		return solutions;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		if (solutions.isEmpty()) {
			return value;
		}

		return solutions.entrySet().stream()
			.map(e -> e.getKey() + " = " + e.getValue())
			.collect(Collectors.joining(" ∧ ", value + " (", ")"));
	}

	/**
	 * Get the String representation of the value of the solution with the
	 * specified name
	 *
	 * @param name
	 *            of solution
	 * @return String representation of solution, or <code>null</code> if no
	 *         solution with that name exists
	 */
	public String getSolution(String name) {
		return solutions.get(name);
	}

	@Override
	public Object getProperty(final String property) {
		try {
			return super.getProperty(property);
		} catch (MissingPropertyException e) {
			if (this.getSolutions().containsKey(property)) {
				return this.getSolution(property);
			} else {
				throw e;
			}
		}
	}

	public <T extends BValue> TranslatedEvalResult<T> translate() throws TranslationException {
		T val = Translator.translate(value);
		Map<String, BValue> sols = new HashMap<>();
		Set<Map.Entry<String, String>> entrySet = solutions.entrySet();
		for (Map.Entry<String, String> entry : entrySet) {
			sols.put(entry.getKey(), Translator.translate(entry.getValue()));
		}
		return new TranslatedEvalResult<>(val, sols);
	}

	/**
	 * Translates the results from ProB into an {@link AbstractEvalResult}. This
	 * is intended mainly for internal use, for developers who are writing
	 * commands and want to translate them into an {@link AbstractEvalResult}.
	 *
	 * @param pt
	 *            PrologTerm
	 * @return {@link AbstractEvalResult} translation of pt
	 */
	public static AbstractEvalResult getEvalResult(PrologTerm pt) {
		if (pt instanceof ListPrologTerm) { // deprecated
			/*
			 * If the evaluation was not successful, the result should be a
			 * Prolog list with the code on the first index and a list of errors
			 * This results therefore in a ComputationNotCompleted command
			 */
			final List<String> strings = PrologTerm.atomicStrings((ListPrologTerm)pt);
			final String code = strings.get(0);
			final List<ErrorItem> errors = strings.subList(1, strings.size()).stream()
				.map(ErrorItem::fromErrorMessage)
				.collect(Collectors.toList());
			return new ComputationNotCompletedResult("Computation not completed", errors, code);
		} else if ("result".equals(pt.getFunctor())) {
			/*
			 * The result term will have the form result(Value,Solutions).
			 * 
			 * If the formula in question was a predicate, Value is
			 * 'TRUE', or 'FALSE' Solutions is then a list of
			 * terms solution(Name,PPSol) where Name is the name of the
			 * free variable calculated by ProB and PPSol is the String pretty
			 * print of the solution calculated by Prolog.
			 *
			 * If the formula in question was an expression,
			 * Value is the string representation of the result calculated by
			 * ProB and Solutions is an empty list.
			 *
			 * From this information, an EvalResult object is created.
			 */

			final CompoundPrologTerm resultTerm = BindingGenerator.getCompoundTerm(pt, 2);
			final String value = PrologTerm.atomicString(resultTerm.getArgument(1));
			final ListPrologTerm solutionList = BindingGenerator.getList(resultTerm.getArgument(2));
			assert "TRUE".equals(value) || "FALSE".equals(value) || solutionList.isEmpty();
			final boolean canCacheResult = solutionList.isEmpty();
			if (canCacheResult) {
				if ("TRUE".equals(value)) {
					return TRUE;
				} else if ("FALSE".equals(value)) {
					return FALSE;
				} else if (formulaCache.containsKey(value)) {
					return formulaCache.get(value);
				}
			}

			final Map<String, String> solutions;
			if (solutionList.isEmpty()) {
				solutions = Collections.emptyMap();
			} else {
				solutions = new HashMap<>();
				for (PrologTerm t : solutionList) {
					CompoundPrologTerm cpt = BindingGenerator.getCompoundTerm(t, "solution", 2);
					solutions.put(PrologTerm.atomicString(cpt.getArgument(1)).intern(), PrologTerm.atomicString(cpt.getArgument(2)).intern());
				}
			}

			EvalResult res = new EvalResult(value, solutions);
			if (canCacheResult) {
				formulaCache.put(value, res);
			}
			return res;
		} else if ("errors".equals(pt.getFunctor())) {
			final CompoundPrologTerm errorsTerm = BindingGenerator.getCompoundTerm(pt, 2);
			final String errorType = PrologTerm.atomicString(errorsTerm.getArgument(1));
			final List<ErrorItem> errors = BindingGenerator.getList(errorsTerm.getArgument(2)).stream()
				.map(term -> {
					if (term.isAtom()) {
						return ErrorItem.fromErrorMessage(PrologTerm.atomicString(term));
					} else {
						return ErrorItem.fromProlog(term);
					}
				})
				.collect(Collectors.toList());
			switch (errorType) {
				case "NOT-WELL-DEFINED":
					return new WDError(errorType, errors);
				
				case "UNKNOWN":
					return new UnknownEvaluationResult(errorType, errors);
				
				case "NOT-INITIALISED":
				case "IDENTIFIER(S) NOT YET INITIALISED; INITIALISE MACHINE FIRST": // deprecated
					return new IdentifierNotInitialised(errorType, errors);
				
				case "ERROR":
				case "SYNTAX ERROR":
				case "TYPE ERROR":
				case "INTERNAL ERROR":
					// TO DO: produce own class
					return new ComputationNotCompletedResult(errorType, errors);
				
				default:
					errors.add(0, new ErrorItem("Unknown error type: " + errorType, ErrorItem.Type.INTERNAL_ERROR, Collections.emptyList()));
					return new ComputationNotCompletedResult(errorType, errors);
			}
		} else if ("enum_warning".equals(pt.getFunctor())) {
			return new EnumerationWarning();
		}
		throw new IllegalArgumentException("Unknown result type " + pt.toString());
	}
}
