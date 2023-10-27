package de.prob.animator.command;

import java.math.BigInteger;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class CheckWellDefinednessCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_check_well_definedness";
	
	private static final String NR_DISCHARGED_VAR = "NrDischarged";
	private static final String NR_TOTAL_VAR = "NrTotal";
	
	private BigInteger dischargedCount;
	private BigInteger totalCount;
	
	public CheckWellDefinednessCommand() {}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(NR_DISCHARGED_VAR);
		pto.printVariable(NR_TOTAL_VAR);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.dischargedCount = BindingGenerator.getAInteger(bindings.get(NR_DISCHARGED_VAR)).getValue();
		this.totalCount = BindingGenerator.getAInteger(bindings.get(NR_TOTAL_VAR)).getValue();
	}
	
	public BigInteger getDischargedCount() {
		return this.dischargedCount;
	}
	
	public BigInteger getTotalCount() {
		return this.totalCount;
	}
}
