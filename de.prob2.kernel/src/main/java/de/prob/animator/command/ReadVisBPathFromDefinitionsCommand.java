package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class ReadVisBPathFromDefinitionsCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_read_visb_path_from_definitions";
	private static final String PATH = "Path";

	private String path;

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(PATH);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		String pathResult = bindings.get(PATH).toString();
		this.path = "none".equals(pathResult) ? null : pathResult;
	}

	public String getPath() {
		return path;
	}
}
