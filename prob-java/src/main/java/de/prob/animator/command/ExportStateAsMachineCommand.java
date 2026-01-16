package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportStateAsMachineCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_export_state_as_machine";
	private final File file;
	private final String stateId;
	private final List<String> identifierIds;

	/**
	 * Export all machine identifiers.
	 */
	public ExportStateAsMachineCommand(final File file, final String stateId) {
		this(file, stateId, new ArrayList<>());
	}

	/**
	 * Export only provided identifiers.
	 */
	public ExportStateAsMachineCommand(final File file, final String stateId, final List<String> identifierIds) {
		this.file = file;
		this.stateId = stateId;
		this.identifierIds = identifierIds;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(file.getAbsolutePath());
		pto.printAtomOrNumber(stateId);
		pto.openList();
		identifierIds.forEach(pto::printAtom);
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}

}
