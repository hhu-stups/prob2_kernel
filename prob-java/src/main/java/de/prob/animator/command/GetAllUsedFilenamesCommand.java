package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import de.prob.animator.domainobjects.MachineFileInformation;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class GetAllUsedFilenamesCommand extends AbstractCommand {
	
	private static final String PROLOG_COMMAND_NAME = "get_machine_files";

	private static final String FILES = "Files";
	
	private final List<MachineFileInformation> files = new ArrayList<>();

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(FILES);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		ListPrologTerm res = (ListPrologTerm) bindings.get(FILES);
		for (PrologTerm prologTerm : res) {
			final String name = prologTerm.getArgument(1).atomToString();
			final String extension = prologTerm.getArgument(2).atomToString();
			final String path = prologTerm.getArgument(3).atomToString();
			files.add(new MachineFileInformation(name, extension, path));
		}
	}

	public List<MachineFileInformation> getFiles() {
		return files;
	}

}
