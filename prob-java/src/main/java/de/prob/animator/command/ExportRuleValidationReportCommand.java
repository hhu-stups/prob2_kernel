package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.io.File;

public class ExportRuleValidationReportCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_export_rule_report";
	private final String stateId;
	private final File file;
	private final long checkTime; // is currently the only option

	public ExportRuleValidationReportCommand(final String stateId, final File file, final long checkTime) {
		this.stateId = stateId;
		this.file = file;
		this.checkTime = checkTime;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(stateId);
		pto.printAtom(file.getAbsolutePath());
		pto.openList();
		pto.openTerm("checktime");
		pto.printNumber(checkTime);
		pto.closeTerm();
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}

}
