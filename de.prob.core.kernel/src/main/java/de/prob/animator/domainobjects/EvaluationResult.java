package de.prob.animator.domainobjects;

import java.util.Arrays;
import java.util.List;

public class EvaluationResult {

	public final String value;
	public final String solution;
	public final String errors;
	public final String code;
	public final String explanation;
	private final String resultType;
	private final List<String> quantifiedVars;

	public EvaluationResult(final String code, final String value,
			final String solution, final String errors, String resultType,
			List<String> quantifiedVars) {
		this.code = code;
		this.value = value;
		this.solution = solution;
		this.errors = errors;
		this.resultType = resultType;
		this.quantifiedVars = quantifiedVars;
		if (!solutionMode(resultType) && "TRUE".equals(value))
			this.explanation = "Solution";
		else
			this.explanation = solutionMode(resultType) ? " Solution: "
					: " Counterexample: ";
	}

	public EvaluationResult(String code, String value, String solution,
			String errors, String resultType, String[] strings) {
		this(code, value, solution, errors, resultType, Arrays.asList(strings));
	}

	private boolean solutionMode(String arg0) {
		return "exists".equals(arg0);
	}

	public String getResultType() {
		return resultType;
	}

	public List<String> getQuantifiedVars() {
		return quantifiedVars;
	}

	@Override
	public String toString() {
		if (!errors.isEmpty())
			return "'Errors: " + errors + "'";
		else {
			if (solution.equals(""))
				return value;
			else
				return value + explanation + solution;
		}
	}

}
