package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetVisBLoadedJsonFileCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_get_loaded_visb_file";
	private static final String RESULT_VARIABLE = "Result";
	
	private String path;

	public GetVisBLoadedJsonFileCommand() {}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		PrologTerm result = bindings.get(RESULT_VARIABLE);
		if (result.hasFunctor("json_file", 1)) {
			this.path = result.getArgument(1).atomToString();
		} else if (result.hasFunctor("none", 0)) {
			this.path = null;
		} else {
			throw new IllegalArgumentException("Expected result json_file/1 or none/0, but got " + result.getFunctor() + "/" + result.getArity());
		}
	}
	
	public String getPath() {
		return path;
	}
}
