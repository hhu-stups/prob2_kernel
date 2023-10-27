package de.prob.animator.command;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.ComputationNotCompletedResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

/**
 * Calculates the values of Classical-B Predicates and Expressions.
 * 
 * @author joy
 * 
 */
public class CbcSolveCommand extends AbstractCommand {
	public enum Solvers {
		PROB, KODKOD, SMT_SUPPORTED_INTERPRETER, Z3, CVC4, CDCLT, SAT, Z3AXM, Z3CNS
	}

	private static final String PROLOG_COMMAND_NAME = "cbc_timed_solve_with_opts";

	private static final int BINDINGS = 1;

	private static final int VAR_NAME = 1;
	@SuppressWarnings("unused")
	private static final int PROLOG_REP = 2;
	private static final int PRETTY_PRINT = 3;

	private static final String EVALUATE_TERM_VARIABLE = "Val";
	private static final String IDENTIFIER_LIST = "IdList";
	private static final String TIME_VARIABLE = "Time";
	private final IEvalElement evalElement;
	private final Solvers solver;
	private final State state;
	private AbstractEvalResult result;
	private BigInteger milliSeconds;
	private final List<String> freeVariables = new ArrayList<>();

	public CbcSolveCommand(final IEvalElement evalElement) {
		this(evalElement, Solvers.PROB);
	}

	public CbcSolveCommand(final IEvalElement evalElement, final State state) {
		this(evalElement, Solvers.PROB, state);
	}
	
	public CbcSolveCommand(final IEvalElement evalElement, final Solvers solver) {
		this(evalElement, solver, null);
	}

	public CbcSolveCommand(final IEvalElement evalElement, final Solvers solver, final State state) {
		this.evalElement = evalElement;
		this.solver = solver;
		this.state = state;
	}

	public AbstractEvalResult getValue() {
		return result;
	}

	public BigInteger getMilliSeconds() {
		return  milliSeconds;
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm idList = bindings.get(IDENTIFIER_LIST);
		if (idList instanceof ListPrologTerm) {
			for (PrologTerm id : (ListPrologTerm) idList) {
				freeVariables.add(id.getFunctor());
			}
		}

		AIntegerPrologTerm runtime =  (AIntegerPrologTerm) bindings.get(TIME_VARIABLE);
		milliSeconds = runtime.getValue();

		PrologTerm prologTerm = bindings.get(EVALUATE_TERM_VARIABLE);

		assert prologTerm instanceof CompoundPrologTerm;

		String functor = prologTerm.getFunctor();

		if ("time_out".equals(functor)) {
			result = new ComputationNotCompletedResult(evalElement.getCode(), "time out");
		} else if ("contradiction_found".equals(functor)) {
			result = EvalResult.FALSE;
		} else if (prologTerm.hasFunctor("solution", 1)) {
			ListPrologTerm solutionBindings = BindingGenerator.getList(prologTerm.getArgument(BINDINGS));

			if (solutionBindings.isEmpty()) {
				result = EvalResult.TRUE;
				return;
			}

			Map<String, String> solutions = new HashMap<>();

			for (PrologTerm b : solutionBindings) {
				CompoundPrologTerm t = (CompoundPrologTerm) b;
				if (t.getArity() == 2) {
					// New format: binding(VarName,PrettyPrint)
					solutions.put(t.getArgument(0).atomToString(), t.getArgument(1).atomToString());
				} else {
					// Old format: binding(VarName,PrologRep,PrettyPrint)
					// The PrologRep is ignored, because it's not usable by the Java side.
					solutions.put(t.getArgument(VAR_NAME).getFunctor(), t.getArgument(PRETTY_PRINT).getFunctor());
				}
			}

			result = new EvalResult("TRUE", solutions);
		} else if (prologTerm.hasFunctor("no_solution_found", 1)) {
			result = new ComputationNotCompletedResult(evalElement.getCode(),
					"no solution found (but one might exist), reason: " + prologTerm.getArgument(1));
		} else if (prologTerm.hasFunctor("error", 0)) {
			// This result doesn't contain any information about the error(s).
			// The Prolog side should only return it after adding errors to the error manager,
			// so the actual error information will be thrown as a ProBError on the Java side.
			result = new ComputationNotCompletedResult(evalElement.getCode(), "Solver returned error(s)");
		} else {
			throw new AssertionError("Unhandled functor in result: " + prologTerm.getFunctor() + "/" + prologTerm.getArity());
		}
	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(PROLOG_COMMAND_NAME);
		pout.printAtom(solver.toString());
		
		pout.openList();
		if (state != null) {
			pout.openTerm("solve_in_visited_state");
			pout.printAtomOrNumber(state.getId());
			pout.closeTerm();
		}
		if (evalElement.expansion() == FormulaExpand.TRUNCATE) {
			pout.printAtom("truncate");
		}
		pout.closeList();
		
		evalElement.printProlog(pout);
		pout.printVariable(IDENTIFIER_LIST);
		pout.printVariable(EVALUATE_TERM_VARIABLE);
		pout.printVariable(TIME_VARIABLE);
		pout.closeTerm();
	}

	public List<String> getFreeVariables() {
		return freeVariables;
	}
}
