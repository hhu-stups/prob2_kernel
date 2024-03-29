/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.command;

import de.prob.check.StateSpaceStats;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.PrologTerm;

public final class ComputeStateSpaceStatsCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "compute_efficient_statespace_stats";
	private StateSpaceStats coverageResult;

	public StateSpaceStats getResult() {
		return coverageResult;
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {

		int nrNodes = ((AIntegerPrologTerm) bindings
				.get("NrNodes")).intValueExact();
		int nrTrans = ((AIntegerPrologTerm) bindings
				.get("NrTrans")).intValueExact();
		int nrProcessed = ((AIntegerPrologTerm) bindings
				.get("NrProcessed")).intValueExact();

		coverageResult = new StateSpaceStats(nrNodes, nrTrans, nrProcessed);

	}

	// NrNodes, NrTrans, NrProcessed
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printVariable("NrNodes")
				.printVariable("NrTrans").printVariable("NrProcessed")
				.closeTerm();
	}

}
