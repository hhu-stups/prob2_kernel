package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class ReadVisBSvgPathCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_file_loaded";
	private static final String JSON_PATH = "JsonFile";
	private static final String SVG_PATH = "SvgFile";

	private String jsonPath;
	private String svgPath;

	public ReadVisBSvgPathCommand() {
		this.jsonPath = null;
		this.svgPath = null;
	}

	/**
	 * @param jsonPath the VisB visualization file path previously passed to {@link LoadVisBCommand} (ignored)
	 * @deprecated Use {@link #ReadVisBSvgPathCommand()} instead. The {@code jsonPath} parameter no longer does anything.
	 */
	@Deprecated
	public ReadVisBSvgPathCommand(final String jsonPath) {
		this();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(JSON_PATH);
		pto.printVariable(SVG_PATH);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.jsonPath = bindings.get(JSON_PATH).atomToString();
		this.svgPath = bindings.get(SVG_PATH).atomToString();
	}

	public String getJsonPath() {
		return this.jsonPath;
	}

	public String getSvgPath() {
		return this.svgPath;
	}
}
