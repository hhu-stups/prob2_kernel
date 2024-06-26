package de.prob.animator.command;

import java.io.File;

import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

/**
 * Loads a Classical B machine that has already been parsed and put into a
 * Recursive Machine Loader.
 * 
 * @author joy
 * 
 */
public class LoadBProjectCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "load_classical_b_from_list_of_facts";

	private final RecursiveMachineLoader rml;
	private final File mainMachine;

	public LoadBProjectCommand(final RecursiveMachineLoader rml, File f) {
		this.rml = rml;
		this.mainMachine = f;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.mainMachine.getAbsolutePath());
		pto.openList();
		this.rml.printAsPrologDirect(pto);
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
