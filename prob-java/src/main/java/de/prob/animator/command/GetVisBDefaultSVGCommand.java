package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class GetVisBDefaultSVGCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_visb_default_svg_file_contents";
	private static final String CONTENTS = "Contents";
	
	private String svgFileContents;
	
	public GetVisBDefaultSVGCommand() {}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(CONTENTS);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.svgFileContents = bindings.get(CONTENTS).atomToString();
	}
	
	public String getSVGFileContents() {
		return this.svgFileContents;
	}
}
