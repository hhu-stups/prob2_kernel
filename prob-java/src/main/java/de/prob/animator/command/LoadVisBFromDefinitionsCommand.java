package de.prob.animator.command;

import java.io.File;

import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class LoadVisBFromDefinitionsCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_load_visb_definitions_from_list_of_facts";

	private final File definitionFile;
	private final RecursiveMachineLoader rml;

	public LoadVisBFromDefinitionsCommand(File definitionFile, RecursiveMachineLoader rml) {
		this.definitionFile = definitionFile;
		this.rml = rml;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.definitionFile.getAbsolutePath());
		pto.openList();
		this.rml.printAsPrologDirect(pto);
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
