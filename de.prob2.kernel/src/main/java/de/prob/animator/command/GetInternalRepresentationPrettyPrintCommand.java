package de.prob.animator.command;

import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

/**
 * @deprecated Use {@link GetInternalRepresentationCommand} instead.
 */
@Deprecated
public class GetInternalRepresentationPrettyPrintCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "get_pretty_print";
	public static final String VARIABLE = "PP";
	private String pp;

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		pp = bindings.get(VARIABLE).atomToString();
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printVariable(VARIABLE)
				.closeTerm();
	}

	public String getPrettyPrint() {
		return pp;
	}
}
