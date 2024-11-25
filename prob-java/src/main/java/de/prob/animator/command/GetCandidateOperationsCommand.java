/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Transition;

/**
 * Get potentially enabled operations.
 * Should be executed in addition to {@link GetEnabledOperationsCommand}.
 */
public final class GetCandidateOperationsCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "compute_additional_candidate_operations_for_state";
	private static final String OPERATIONS_VARIABLE = "Candidates";

	private final String id;
	private final List<Candidate> candidates = new ArrayList<>();

	public GetCandidateOperationsCommand(String id) {
		this.id = id;
	}

	public static final class Candidate {

		private final String operation;
		private final boolean timeoutOccurred;
		private final CandidateGuardPrecision guardPrecision;

		public Candidate(String operation, boolean timeoutOccurred, CandidateGuardPrecision guardPrecision) {
			this.operation = operation;
			this.timeoutOccurred = timeoutOccurred;
			this.guardPrecision = guardPrecision;
		}

		public static Candidate fromProlog(PrologTerm t) {
			CompoundPrologTerm cpt = BindingGenerator.getCompoundTerm(t, "candidate", 3);
			String operation = Transition.getIdFromPrologTerm(cpt.getArgument(1));
			boolean timeoutOccurred = "true".equals(cpt.getArgument(2).atomToString());
			CandidateGuardPrecision guardPrecision = CandidateGuardPrecision.fromProlog(cpt.getArgument(3));
			return new Candidate(operation, timeoutOccurred, guardPrecision);
		}

		public String getOperation() {
			return this.operation;
		}

		public boolean getTimeoutOccurred() {
			return this.timeoutOccurred;
		}

		public CandidateGuardPrecision getGuardPrecision() {
			return this.guardPrecision;
		}
	}

	public enum CandidateGuardPrecision {
		PRECISE, IMPRECISE;

		public static CandidateGuardPrecision fromProlog(PrologTerm t) {
			String atom = t.atomToString();
			switch (atom) {
				case "precise":
					return PRECISE;
				case "imprecise":
					return IMPRECISE;
				default:
					throw new IllegalArgumentException("Unknown prolog atom: " + atom);
			}
		}
	}

	// candidate(OpName,TimeOutOccurred,GuardPrecise)
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		List<Candidate> candidates = BindingGenerator.getList(bindings, OPERATIONS_VARIABLE).stream()
				.map(Candidate::fromProlog)
				.collect(Collectors.toList());
		this.candidates.addAll(candidates);
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(id);
		pto.printVariable(OPERATIONS_VARIABLE);
		pto.closeTerm();
	}

	public List<Candidate> getCandidates() {
		return this.candidates;
	}
}
