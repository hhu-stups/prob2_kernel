package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class ReadVisBSvgPathCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_file_loaded";
	private static final String SVG_PATH = "SvgFile";
	private final String jsonPath;
	private String svgPath;

	public ReadVisBSvgPathCommand(final String jsonPath) {
		this.jsonPath = jsonPath;
		this.svgPath = null;
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printString(jsonPath);
		pto.printVariable(SVG_PATH);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.svgPath = bindings.get(SVG_PATH).toString();
	}

	public String getSvgPath() {
		return SVG_PATH;
	}
}
